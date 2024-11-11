package sit.int221.kanbanapi.jwts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.kanbandb.repositories.CollabRepository;
import sit.int221.kanbanapi.exceptions.*;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.CollabService;
import sit.int221.kanbanapi.services.JwtTokenUtil;
import sit.int221.kanbanapi.services.JwtUserDetailsService;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.List;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BoardService boardService;

    @Autowired
    private CollabRepository collabRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        String boardId = extractBoardIdFromURI(requestURI);

        if (requestURI.equals("/login")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;

            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);

                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);

                    String tokenType = claims.get("token_type", String.class);
                    if (!tokenType.equals("access_token") && !requestURI.equals("/token")) {
                        throw new AuthenticationFailed("Invalid token type");
                    }

                    String userOid = claims.get("oid", String.class);

                    if (boardId != null) {
                        validateBoardAccess(requestMethod, boardId, userOid, requestURI);
                    }

                    setAuthenticationIfValid(request, username, jwtToken);
                } catch (AuthenticationFailed ex) {
                    throw new AuthenticationFailed(ex.getMessage());
                } catch (ItemNotFoundException ex) {
                    throw new ItemNotFoundException(ex.getMessage());
                } catch (CollaboratorConflict ex) {
                    throw new CollaboratorConflict(ex.getMessage());
                } catch (NoPermission ex) {
                    throw new NoPermission(ex.getMessage());
                } catch (Exception ex) {
                    handleTokenException(requestMethod, boardId, request, response, chain, ex);
                    return;
                }

            } else if (requestMethod.equals("GET")) {
                if (boardId != null) {
                    Board board = boardService.getBoardById(boardId);
                    if (board.getVisibility().equals("PUBLIC")) {
                        chain.doFilter(request, response);
                        return;
                    } else {
                        throw new NoPermission("You do not have permission to perform this action.");
                    }
                }
            } else {
                throw new AuthenticationFailed("Authorization header missing");
            }

            chain.doFilter(request, response);

        } catch (AuthenticationFailed | ItemNotFoundException | CollaboratorConflict | NoPermission ex) {
            buildErrorResponse(response, ex, getHttpStatus(ex), request);
        }
    }

    private void validateBoardAccess(String requestMethod, String boardId, String userOid, String requestURI) {
        Board board = boardService.getBoardById(boardId);

        if (board.getOwnerId().equals(userOid)) {
            return;
        } else if (board.getVisibility().equals("PUBLIC") && requestMethod.equals("GET")) {
            return;
        } else if (isInviteRequest(requestURI)) {
            return;
        } else {
            if (isSelfCollabRemovalRequest(requestMethod, requestURI, userOid)) {
                return;
            }
            Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, userOid)
                    .orElseThrow(() -> new NoPermission("You do not have permission to perform this action"));
            String accessRight = collab.getAccessRight().toString();

            if (collab.getStatus().equals(Collab.Status.PENDING)) {
                throw new NoPermission("You do not have permission to perform this action");
            }

            if (isTaskOrStatusOrCollabRequest(requestURI)) {
                if (accessRight.equals("WRITE")) {
                    if (isCollabRequest(requestURI) && !requestMethod.equals("GET")) {
                        throw new NoPermission("You do not have permission to perform this action");
                    }
                    return;
                } else if (accessRight.equals("READ") && !requestMethod.equals("GET")) {
                    throw new NoPermission("You do not have permission to perform this action.");
                }
            } else if (requestMethod.equals("GET")) {
                return;
            } else {
                throw new NoPermission("You do not have permission to manage the board.");
            }
        }
    }

    private boolean isTaskOrStatusOrCollabRequest(String requestURI) {
        return requestURI.matches(".*/boards/[a-zA-Z0-9_-]+/(tasks|statuses|collabs)(/.*)?");
    }

    private boolean isSelfCollabRemovalRequest(String requestMethod, String requestURI, String userOid) {
        return (requestMethod.equals("DELETE") && requestURI.matches(".*/boards/[a-zA-Z0-9_-]+/collabs/" + userOid));
    }

    private boolean isCollabRequest(String requestURI) {
        return requestURI.matches(".*/boards/[a-zA-Z0-9_-]+/collabs(/.*)?");
    }

    private boolean isInviteRequest(String requestURI) {
        return requestURI.matches(".*/boards/[a-zA-Z0-9_-]+/collabs/invitations");
    }

    private String extractBoardIdFromURI(String requestURI) {
        String[] parts = requestURI.split("/");
        if (parts.length >= 4 && parts[2].equals("boards")) {
            return parts[3];
        }
        return null;
    }


    private void setAuthenticationIfValid(HttpServletRequest request, String username, String jwtToken) {
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                setAuthentication(request, userDetails);
            }
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void handleTokenException(String requestMethod, String boardId,
                                      HttpServletRequest request, HttpServletResponse response,
                                      FilterChain chain, Exception ex)
            throws IOException, ServletException {

        if (requestMethod.equals("GET") && boardId != null) {
            Board board = boardService.getBoardById(boardId);
            if (board.getVisibility().equals("PUBLIC")) {
                chain.doFilter(request, response);
                return;
            }
        }

        if (ex instanceof ExpiredJwtException) {
            throw new AuthenticationFailed("JWT Token has expired");
        } else if (ex instanceof MalformedJwtException) {
            throw new AuthenticationFailed("Malformed JWT token");
        } else if (ex instanceof UnsupportedJwtException) {
            throw new AuthenticationFailed("Unsupported JWT token");
        } else if (ex instanceof SignatureException) {
            throw new AuthenticationFailed("Invalid JWT signature");
        } else if (ex instanceof IllegalArgumentException) {
            throw new AuthenticationFailed("JWT token compact of handler are invalid");
        }

        throw new AuthenticationFailed(ex.getMessage());
    }

    private void buildErrorResponse(HttpServletResponse response, Exception exception,
                                    HttpStatus httpStatus, HttpServletRequest request) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), exception.getMessage(), request.getRequestURI());
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(errorResponse));
    }

    private HttpStatus getHttpStatus(Exception ex) {
        if (ex instanceof NoPermission) {
            return HttpStatus.FORBIDDEN;
        } else if (ex instanceof ItemNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof CollaboratorConflict) {
            return HttpStatus.CONFLICT;
        } else {
            return HttpStatus.UNAUTHORIZED;
        }
    }
}
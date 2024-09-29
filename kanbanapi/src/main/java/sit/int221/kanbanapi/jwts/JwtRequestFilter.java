package sit.int221.kanbanapi.jwts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
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
import sit.int221.kanbanapi.exceptions.*;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.JwtTokenUtil;
import sit.int221.kanbanapi.services.JwtUserDetailsService;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BoardService boardService;

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
                        boardService.checkBoardOwnership(boardId, requestMethod, userOid);
                    }
                } catch (ItemNotFoundException ex) {
                    throw new ItemNotFoundException(ex.getMessage());
                } catch (NoPermission ex) {
                    throw new NoPermission(ex.getMessage());
                } catch (ExpiredJwtException ex) {
                    if (requestMethod.equals("GET")) {
                        if (boardId != null) {
                            Board board = boardService.getBoardById(boardId);
                            if (board.getVisibility().equals("PUBLIC")) {
                                chain.doFilter(request, response);
                                return;
                            }
                        }
                    } else {
                        throw new AuthenticationFailed("JWT Token has expired");
                    }
                } catch (Exception ex) {
                    if (requestMethod.equals("GET")) {
                        if (boardId != null) {
                            Board board = boardService.getBoardById(boardId);
                            if (board.getVisibility().equals("PUBLIC")) {
                                chain.doFilter(request, response);
                                return;
                            }
                        }
                    } else {
                        throw new AuthenticationFailed(ex.getMessage());
                    }
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

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            chain.doFilter(request, response);
        } catch (AuthenticationFailed ex) {
            buildErrorResponse(response, ex, HttpStatus.UNAUTHORIZED, request);
        } catch (ItemNotFoundException ex) {
            buildErrorResponse(response, ex, HttpStatus.NOT_FOUND, request);
        } catch (NoPermission ex) {
            buildErrorResponse(response, ex, HttpStatus.FORBIDDEN, request);
        } catch (Exception ex) {
            buildErrorResponse(response, ex, HttpStatus.UNAUTHORIZED, request);
        }
    }

    private String extractBoardIdFromURI(String requestURI) {
        String[] parts = requestURI.split("/");
        if (parts.length >= 4 && parts[2].equals("boards")) {
            return parts[3];
        }
        return null;
    }

    private void buildErrorResponse(HttpServletResponse response, Exception exception,
                                    HttpStatus httpStatus, HttpServletRequest request) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), exception.getMessage(), request.getRequestURI());
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(errorResponse));
    }
}
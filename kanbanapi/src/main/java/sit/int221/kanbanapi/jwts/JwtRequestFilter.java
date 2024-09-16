package sit.int221.kanbanapi.jwts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ErrorResponse;
import sit.int221.kanbanapi.services.JwtTokenUtil;
import sit.int221.kanbanapi.services.JwtUserDetailsService;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;

            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    } catch (IllegalArgumentException e) {
                        throw new AuthenticationFailed("Unable to get JWT Token");
                    } catch (ExpiredJwtException e) {
                        throw new AuthenticationFailed("JWT Token has expired");
                    }
                } else {
                    throw new AuthenticationFailed("JWT Token does not begin with Bearer String");
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            chain.doFilter(request, response);
        } catch (AuthenticationFailed ex) {
            buildErrorResponse(response, ex, HttpStatus.UNAUTHORIZED, request);
        }
    }

    private void buildErrorResponse(HttpServletResponse response,
                                    Exception exception, HttpStatus httpStatus, HttpServletRequest request) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), exception.getMessage(), request.getRequestURI());
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(errorResponse));
    }
}
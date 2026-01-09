package com.app.filter;

import com.app.util.JWTUtil;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String requestURI = httpRequest.getRequestURI();
        
        if (requestURI.contains("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie c : httpRequest.getCookies()) {
                if ("authToken".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        try {
            if (token == null || !JWTUtil.isTokenValid(token)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized\"}");
                System.out.println("The token is invalid or missing: " + token);
                return; 
            }
            DecodedJWT jwtToken = JWTUtil.validateToken(token);
            int userId = JWTUtil.getUserId(jwtToken);
            String role = JWTUtil.getRole(jwtToken);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            chain.doFilter(request, response);

        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("Token validation error: " + e.getMessage());
        }
    }
}
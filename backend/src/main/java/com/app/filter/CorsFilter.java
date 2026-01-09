package com.app.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;

@WebFilter("/*")
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 1. Allow your frontend URL
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        // 2. REQUIRED: Allow Cookies/Credentials (This fixes your error)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 3. Allow methods
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 4. Allow headers
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // 5. Handle "Preflight" OPTIONS requests
        // The browser asks "Can I post?" before sending data. We must say "Yes" (200 OK) immediately.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Stop here, don't continue to the servlet
        }

        chain.doFilter(req, res);
    }
}
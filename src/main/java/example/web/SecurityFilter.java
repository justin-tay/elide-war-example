package example.web;

import java.io.IOException;
import java.security.Principal;

import example.security.SecurityContext;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SecurityFilter for testing purposes only.
 */
@WebFilter
public class SecurityFilter implements Filter {

    @Inject
    SecurityContext securityContext;

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        httpResponse.setHeader("Cache-Control", "no-cache");

        String requestURI = httpRequest.getRequestURI();
        if("/logout".equals(requestURI)) {
            Cookie cookie = new Cookie("user", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            httpResponse.addCookie(cookie);
            httpResponse.sendRedirect("/");
            return;
        }
        else if("/j_security_check".equals(requestURI)) {
            String username = (String) httpRequest.getParameter("j_username");
            Cookie cookie = new Cookie("user", username);
            cookie.setPath("/");
            httpResponse.addCookie(cookie);
            httpResponse.sendRedirect("/");
            return;
        }
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null) {
            this.filterConfig.getServletContext().getRequestDispatcher("/login.html").forward(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

}

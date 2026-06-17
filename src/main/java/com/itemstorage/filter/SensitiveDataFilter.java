package com.itemstorage.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class SensitiveDataFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveDataFilter.class);

    private static final Set<String> SENSITIVE_URIS = Set.of(
            "/api/inventory/create",
            "/receptionist/create",
            "/patients/import",
            "/discharge/import",
            "/admin/users/create"
    );

    private static final Set<String> IGNORED_PREFIXES = Set.of(
            "/css/", "/js/", "/images/", "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();

        for (String prefix : IGNORED_PREFIXES) {
            if (uri.startsWith(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        boolean isSensitive = false;
        for (String sensitiveUri : SENSITIVE_URIS) {
            if (uri.equals(sensitiveUri) || uri.startsWith(sensitiveUri + "/")) {
                isSensitive = true;
                break;
            }
        }

        if (isSensitive) {
            log.debug("Request: {} {} [SENSITIVE DATA - NOT LOGGED]", req.getMethod(), uri);
        } else {
            log.debug("Request: {} {}?{}", req.getMethod(), uri,
                    req.getQueryString() != null ? req.getQueryString() : "");
        }

        chain.doFilter(request, response);
    }
}
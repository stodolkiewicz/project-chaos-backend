package com.stodo.projectchaos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/api/debug-info")
    public Map<String, String> getRequestInfo(HttpServletRequest request) {
        Map<String, String> info = new HashMap<>();

        // --- Kluczowe informacje o tym, co "widzi" Spring ---
        info.put("=== SPRING_SEES_SCHEME ===", request.getScheme());
        info.put("=== SPRING_SEES_SERVER_NAME ===", request.getServerName());
        info.put("=== SPRING_SEES_SERVER_PORT ===", String.valueOf(request.getServerPort()));

        // --- Nagłówki, które faktycznie dotarły do aplikacji ---
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Szukamy tylko tych najważniejszych
                if (headerName.toLowerCase().contains("forwarded") || headerName.toLowerCase().contains("host")) {
                    info.put(headerName, request.getHeader(headerName));
                }
            }
        }
        return info;
    }
}
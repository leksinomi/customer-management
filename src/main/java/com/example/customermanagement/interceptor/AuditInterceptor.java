package com.example.customermanagement.interceptor;

import com.example.customermanagement.annotation.Auditable;
import com.example.customermanagement.service.AuditService;
import com.example.customermanagement.filter.CachedBodyHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<AuditContext> AUDIT_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
    private static final String HANDLED_EXCEPTION_ATTRIBUTE = "HANDLED_EXCEPTION";
    private static final String CUSTOMER_ID_PARAM = "id";

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String action = getActionFromHandler(handler);
        if (action != null) {
            String requestBody = getRequestBody(request);
            Long customerId = getCustomerIdFromRequest(request);
            AUDIT_CONTEXT_THREAD_LOCAL.set(new AuditContext(action, customerId, requestBody));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuditContext auditContext = AUDIT_CONTEXT_THREAD_LOCAL.get();
        if (auditContext != null) {
            String status = determineStatus(ex, request);
            auditService.createAuditEntry(
                    auditContext.action(),
                    auditContext.customerId(),
                    auditContext.requestBody(),
                    status
            );
            AUDIT_CONTEXT_THREAD_LOCAL.remove();
        }
    }

    private String getActionFromHandler(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            Auditable auditable = handlerMethod.getMethodAnnotation(Auditable.class);
            return Optional.ofNullable(auditable)
                    .map(Auditable::action)
                    .orElse(null);
        }
        return null;
    }

    private String determineStatus(Exception ex, HttpServletRequest request) {
        return Optional.ofNullable(ex)
                .orElse((Exception) request.getAttribute(HANDLED_EXCEPTION_ATTRIBUTE)) == null
                ? "SUCCESS" : "FAILED";
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
            String requestBody = cachedRequest.getReader().lines()
                    .collect(Collectors.joining())
                    .replaceAll("\\s+", "");
            return requestBody.isEmpty() ? null : requestBody;
        }
        return null;
    }

    private Long getCustomerIdFromRequest(HttpServletRequest request) {
        Map<String, String> uriTemplateVars =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return Optional.ofNullable(uriTemplateVars)
                .map(vars -> vars.get(CUSTOMER_ID_PARAM))
                .map(Long::valueOf)
                .orElse(null);
    }

    private record AuditContext(String action, Long customerId, String requestBody) {}
}

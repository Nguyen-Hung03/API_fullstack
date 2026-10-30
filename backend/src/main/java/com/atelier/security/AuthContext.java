package com.atelier.security;

import com.atelier.domain.Customer;
import com.atelier.domain.Role;
import com.atelier.error.ApiException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class AuthContext {

    public static final String ATTR = "authCustomer";

    private AuthContext() {
    }

    public static Customer optional() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        Object value = attrs.getRequest().getAttribute(ATTR);
        return value instanceof Customer customer ? customer : null;
    }

    public static void requireSelf(Long customerId) {
        if (!httpRequest()) {
            return;
        }
        Customer current = currentOrUnauthorized();
        if (current.getRole() != Role.ADMIN && !current.getId().equals(customerId)) {
            throw ApiException.forbidden("Không có quyền với tài khoản này");
        }
    }

    public static void requireAdmin() {
        if (!httpRequest()) {
            return;
        }
        Customer current = currentOrUnauthorized();
        if (current.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Chỉ quản trị viên được thực hiện");
        }
    }

    public static void requireOwnerOrAdmin(Long customerId) {
        requireSelf(customerId);
    }

    private static Customer currentOrUnauthorized() {
        Customer current = optional();
        if (current == null) {
            throw ApiException.unauthorized("Cần đăng nhập");
        }
        return current;
    }

    private static boolean httpRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes;
    }
}

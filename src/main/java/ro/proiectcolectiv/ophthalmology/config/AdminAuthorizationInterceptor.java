package ro.proiectcolectiv.ophthalmology.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ro.proiectcolectiv.ophthalmology.exception.ApiException;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final boolean authorizationEnabled;
    private final String userHeader;
    private final Set<String> allowedUsers;

    public AdminAuthorizationInterceptor(
            @Value("${app.admin.authorization-enabled}") boolean authorizationEnabled,
            @Value("${app.admin.user-header}") String userHeader,
            @Value("${app.admin.allowed-users}") String allowedUsers) {
        this.authorizationEnabled = authorizationEnabled;
        this.userHeader = userHeader;
        this.allowedUsers = Arrays.stream(allowedUsers.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!authorizationEnabled) {
            return true;
        }

        String currentUser = request.getHeader(userHeader);
        if (currentUser == null || currentUser.isBlank()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Lipseste userul preautentificat pentru zona admin.");
        }

        if (!allowedUsers.contains(currentUser.trim().toLowerCase())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Userul curent nu are drepturi de admin.");
        }

        return true;
    }
}

package finance.management.controller;

import finance.management.dto.request.LoginRequest;
import finance.management.dto.request.RegisterRequest;
import finance.management.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    @Test
    void register_returns201() {
        RegisterRequest req = new RegisterRequest();
        when(authService.register(any())).thenReturn(Map.of("message", "User registered successfully", "userId", 1L));

        ResponseEntity<Map<String, Object>> response = authController.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("message", "User registered successfully");
    }

    @Test
    void login_returns200() {
        LoginRequest req = new LoginRequest();
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);
        when(authService.login(any(), any(), any())).thenReturn(Map.of("message", "Login successful"));

        ResponseEntity<Map<String, Object>> response = authController.login(req, httpReq, httpRes);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Login successful");
    }

    @Test
    void logout_returns200() {
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);
        when(authService.logout(any(), any())).thenReturn(Map.of("message", "Logout successful"));

        ResponseEntity<Map<String, Object>> response = authController.logout(httpReq, httpRes);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Logout successful");
    }
}

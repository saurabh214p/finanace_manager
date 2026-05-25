package finance.management.service;

import finance.management.dto.request.LoginRequest;
import finance.management.dto.request.RegisterRequest;
import finance.management.entity.User;
import finance.management.exception.ConflictException;
import finance.management.repository.UserRepository;
import finance.management.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");
    }

    @Test
    void register_success() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");

        User saved = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(saved);

        Map<String, Object> result =
                authService.register(registerRequest);

        assertThat(result.get("message"))
                .isEqualTo("User registered successfully");

        assertThat(result.get("userId"))
                .isEqualTo(1L);

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_conflict_whenUsernameExists() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(registerRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_success() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(
                        new UsernamePasswordAuthenticationToken(
                                "test@example.com",
                                "password123"
                        )
                );

        HttpServletRequest httpRequest =
                mock(HttpServletRequest.class);

        HttpServletResponse httpResponse =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        when(httpRequest.getSession(true))
                .thenReturn(session);

        Map<String, Object> result =
                authService.login(
                        loginRequest,
                        httpRequest,
                        httpResponse
                );

        assertThat(result.get("message"))
                .isEqualTo("Login successful");
    }

    @Test
    void logout_success() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        when(request.getSession(false))
                .thenReturn(session);

        Map<String, Object> result =
                authService.logout(request, response);

        assertThat(result.get("message"))
                .isEqualTo("Logout successful");

        verify(session).invalidate();
    }

    @Test
    void logout_noSession() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        when(request.getSession(false))
                .thenReturn(null);

        Map<String, Object> result =
                authService.logout(request, response);

        assertThat(result.get("message"))
                .isEqualTo("Logout successful");
    }
}
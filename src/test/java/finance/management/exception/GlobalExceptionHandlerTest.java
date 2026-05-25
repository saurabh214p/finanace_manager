package finance.management.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleBadRequest_returns400() {

        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(
                        new BadRequestException("invalid input")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .containsEntry("error", "invalid input");
    }

    @Test
    void handleNotFound_returns404() {

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("not found")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .containsEntry("error", "not found");
    }

    @Test
    void handleConflict_returns409() {

        ResponseEntity<Map<String, Object>> response =
                handler.handleConflict(
                        new ConflictException("already exists")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .containsEntry("error", "already exists");
    }

    @Test
    void handleForbidden_returns403() {

        ResponseEntity<Map<String, Object>> response =
                handler.handleForbidden(
                        new ForbiddenException("access denied")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(response.getBody())
                .containsEntry("error", "access denied");
    }

    @Test
    void handleValidationErrors_returns400WithMessages() {

        FieldError fieldError =
                new FieldError(
                        "obj",
                        "field",
                        "must not be blank"
                );

        BindingResult bindingResult =
                mock(BindingResult.class);

        doReturn(List.of(fieldError))
                .when(bindingResult)
                .getFieldErrors();

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(
                        null,
                        bindingResult
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()
                .get("error")
                .toString())
                .contains("must not be blank");
    }

    @Test
    void handleUnreadableMessage_returns400() {

        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("invalid json");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnreadableMessage(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()
                .get("error")
                .toString())
                .contains("Invalid request body");
    }

    @Test
    void handleGeneral_returns500() {

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneral(
                        new RuntimeException("unexpected")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .containsEntry(
                        "error",
                        "An unexpected error occurred"
                );
    }
}
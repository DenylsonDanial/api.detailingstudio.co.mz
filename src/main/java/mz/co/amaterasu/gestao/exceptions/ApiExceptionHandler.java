package mz.co.amaterasu.gestao.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;

@ControllerAdvice
public class ApiExceptionHandler {

	Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(value = {ApiRequestException.class})
	public ResponseEntity<Object> handlerApiRequestException(ApiRequestException e) {
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;

		ApiException apiException =
				new ApiException(e.getMessage(), badRequest, ZonedDateTime.now(ZoneId.of("Z")));
		return new ResponseEntity<>(apiException, badRequest);
	}

	@ExceptionHandler(value = {DataIntegrityViolationException.class})
	public ResponseEntity<Object> handlerApiRequestException(DataIntegrityViolationException e) {
		logger.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;


		ApiException apiException =
				new ApiException("O item não pode ser removido ou modificado", badRequest, ZonedDateTime.now(ZoneId.of("Z")));
		return new ResponseEntity<>(apiException, badRequest);
	}

	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	public ResponseEntity<Object> handlerApiRequestValidationException(MethodArgumentNotValidException e) {
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		Map<String, Map<String, String>> preparedError = new HashMap<>();
		preparedError.put("validation", errors);
		return new ResponseEntity<>(preparedError, badRequest);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleSecurityException(Exception exception) {
		ProblemDetail errorDetail = null;

        logger.warn("Security exception: {}", exception.getMessage());

		if (exception instanceof BadCredentialsException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "O nome de utilizador ou a senha estão incorretos.");

			return errorDetail;
		}

		if (exception instanceof AccountStatusException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "Esta conta está bloqueada.");
		}

		if (exception instanceof AccessDeniedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "Você não tem permissão para acessar este recurso.");
		}

		if (exception instanceof SignatureException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "A assinatura do token JWT é inválida.");
		}

		if (exception instanceof ExpiredJwtException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "O token JWT expirou. Faça login novamente.");
		}

		if (errorDetail == null) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
			errorDetail.setProperty("description", "Ocorreu um erro interno. Por favor, tente novamente mais tarde.");
		}

		return errorDetail;
	}
}

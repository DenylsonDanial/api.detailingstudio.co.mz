package mz.co.amaterasu.gestao.exceptions;

import java.io.Serial;

/**
 * Erro de negocio devolvido ao cliente com uma mensagem legivel.
 * E unchecked de proposito: os servicos lancam-na livremente e o
 * {@link ApiExceptionHandler} traduz para a resposta HTTP.
 */
public class ApiRequestException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ApiRequestException(String msg) {
		super(msg);
	}

	public ApiRequestException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

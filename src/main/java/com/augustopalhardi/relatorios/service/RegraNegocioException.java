package com.augustopalhardi.relatorios.service;

/**
 * Violacao de uma regra de negocio. A mensagem e escrita para o usuario final e
 * pode ser exibida diretamente na tela.
 */
public class RegraNegocioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RegraNegocioException(String mensagemParaUsuario) {
		super(mensagemParaUsuario);
	}

	/** Lanca a excecao se a mensagem de erro nao for null. */
	public static void seHouver(String mensagemDeErro) {
		if (mensagemDeErro != null) {
			throw new RegraNegocioException(mensagemDeErro);
		}
	}
}

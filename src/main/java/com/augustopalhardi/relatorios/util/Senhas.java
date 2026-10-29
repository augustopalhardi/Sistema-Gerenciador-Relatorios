package com.augustopalhardi.relatorios.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Regras de senha: geracao e conferencia de hash BCrypt e validacao de uma
 * nova senha. A senha nunca e aparada (espacos fazem parte dela).
 */
public final class Senhas {

	public static final int TAMANHO_MINIMO = 6;

	private Senhas() {
	}

	/** Gera o hash BCrypt de uma senha em texto puro. */
	public static String gerarHash(String senha) {
		if (senha == null || senha.isEmpty()) {
			throw new IllegalArgumentException("Senha vazia");
		}
		return BCrypt.hashpw(senha, BCrypt.gensalt());
	}

	/**
	 * Confere a senha digitada com o hash gravado. Devolve false, sem lancar
	 * excecao, se algum dos dois estiver ausente ou se o valor gravado nao for
	 * um hash BCrypt valido.
	 */
	public static boolean confere(String senha, String hash) {
		if (senha == null || senha.isEmpty() || !ehHash(hash)) {
			return false;
		}
		try {
			return BCrypt.checkpw(senha, hash);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/** Indica se o valor tem o formato de um hash BCrypt. */
	public static boolean ehHash(String valor) {
		return valor != null && valor.length() == 60
				&& (valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$"));
	}

	/**
	 * Valida uma nova senha e sua confirmacao. Devolve a mensagem de erro para o
	 * usuario, ou null se a senha for aceita. Passe confirmacao null quando a
	 * tela nao tiver campo de confirmacao.
	 */
	public static String validarNova(String senha, String confirmacao) {
		if (senha == null || senha.isEmpty()) {
			return "Informe a senha.";
		}
		if (senha.trim().isEmpty()) {
			return "A senha não pode ser formada só por espaços.";
		}
		if (senha.length() < TAMANHO_MINIMO) {
			return "A senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres.";
		}
		if (confirmacao != null && !senha.equals(confirmacao)) {
			return "A confirmação da senha não confere.";
		}
		return null;
	}
}

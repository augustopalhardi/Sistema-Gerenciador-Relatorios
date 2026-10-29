package com.augustopalhardi.relatorios.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Regras de acesso por URL usadas pelo {@link AuthenticationFilter}. Funcoes
 * puras sobre o caminho da requisicao (sem o context path).
 */
public final class RegrasAcesso {

	public static final String PAGINA_LOGIN = "/index.xhtml";
	public static final String PAGINA_INICIAL = "/home/home.xhtml";

	/** Telas disponiveis para qualquer usuario autenticado. */
	static final List<String> PAGINAS_PRIVADAS = Collections.unmodifiableList(Arrays.asList(
			PAGINA_INICIAL,
			"/conta/minhaConta.xhtml",
			"/relatorio/Relatorio.xhtml",
			"/relatorio/Rascunhos.xhtml",
			"/relatorio/RascunhosFormatados.xhtml",
			"/relatorio/VerificarRelatorio.xhtml",
			"/empresa/DadosEmpresa.xhtml",
			"/usuario/CadastroUsuario.xhtml",
			"/cliente/CadastroCliente.xhtml"));

	private RegrasAcesso() {
	}

	/** Recursos e paginas acessiveis sem login (login, erros, CSS/JS/imagens do JSF). */
	public static boolean ehPublico(String caminho) {
		return PAGINA_LOGIN.equals(caminho) || "/".equals(caminho) || caminho.startsWith("/error/")
				|| caminho.startsWith("/javax.faces.resource/");
	}

	/** Paginas que um usuario autenticado pode abrir. */
	public static boolean ehPermitido(String caminho) {
		return PAGINAS_PRIVADAS.contains(caminho);
	}

	/** Pagina de login (para mandar quem ja esta logado direto para o painel). */
	public static boolean ehLogin(String caminho) {
		return PAGINA_LOGIN.equals(caminho) || "/".equals(caminho);
	}
}

package com.augustopalhardi.relatorios.view;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.augustopalhardi.relatorios.model.CadastroUsuario;

/**
 * Acesso ao usuario autenticado, guardado na sessao HTTP pelo login. O mesmo
 * atributo e lido pelo AuthenticationFilter e pelo ImageServlet, e fica
 * disponivel nas telas como #{usuarioLogado}.
 */
public final class SessaoUsuario {

	public static final String ATRIBUTO = "usuarioLogado";

	private SessaoUsuario() {
	}

	/** Usuario logado, ou null se nao houver sessao. */
	public static CadastroUsuario atual() {
		HttpSession sessao = sessao(false);
		return sessao != null ? (CadastroUsuario) sessao.getAttribute(ATRIBUTO) : null;
	}

	/** Nome do usuario logado, ou o padrao se nao houver. */
	public static String nomeOuPadrao(String padrao) {
		CadastroUsuario u = atual();
		return u != null && u.getNome() != null ? u.getNome() : padrao;
	}

	public static void definir(CadastroUsuario usuario) {
		sessao(true).setAttribute(ATRIBUTO, usuario);
	}

	/** Guarda um valor temporario na sessao (ex.: ID a abrir na proxima tela). */
	public static void guardar(String chave, Object valor) {
		sessao(true).setAttribute(chave, valor);
	}

	/** Le e remove um valor temporario guardado com {@link #guardar}. */
	public static Object retirar(String chave) {
		HttpSession sessao = sessao(false);
		if (sessao == null) {
			return null;
		}
		Object valor = sessao.getAttribute(chave);
		sessao.removeAttribute(chave);
		return valor;
	}

	private static HttpSession sessao(boolean criar) {
		return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(criar);
	}
}

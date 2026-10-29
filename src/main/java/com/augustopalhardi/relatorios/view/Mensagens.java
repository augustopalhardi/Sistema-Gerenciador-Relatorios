package com.augustopalhardi.relatorios.view;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Padrao unico de mensagens das telas (exibidas pelo p:growl do layout):
 * sucesso, aviso e erro, sempre com titulo curto e detalhe.
 */
public final class Mensagens {

	/** Mensagem generica para falhas inesperadas; o detalhe tecnico vai so para o log. */
	public static final String ERRO_INESPERADO = "Não foi possível concluir a operação. Tente novamente; se o problema continuar, contate o administrador.";

	private Mensagens() {
	}

	public static void sucesso(String detalhe) {
		adicionar(FacesMessage.SEVERITY_INFO, "Sucesso", detalhe);
	}

	public static void aviso(String detalhe) {
		adicionar(FacesMessage.SEVERITY_WARN, "Atenção", detalhe);
	}

	public static void erro(String detalhe) {
		adicionar(FacesMessage.SEVERITY_ERROR, "Erro", detalhe);
	}

	/** Erro inesperado: mensagem generica para o usuario. */
	public static void erroInesperado() {
		erro(ERRO_INESPERADO);
	}

	private static void adicionar(FacesMessage.Severity severidade, String titulo, String detalhe) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, titulo, detalhe));
	}
}

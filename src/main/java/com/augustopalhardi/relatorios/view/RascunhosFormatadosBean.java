package com.augustopalhardi.relatorios.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.model.RelatorioFormatado;
import com.augustopalhardi.relatorios.service.FiltroRelatorios;
import com.augustopalhardi.relatorios.service.ManterRelatorioFormatadoService;

/** Tela "Rascunhos Formatados": documentos ja diagramados, prontos para reabrir no editor. */
@Named
@ViewScoped
public class RascunhosFormatadosBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RascunhosFormatadosBean.class);

	/** Chave de sessao com o ID que o editor deve abrir. */
	static final String CHAVE_RASCUNHO_A_ABRIR = "rascunhoFormatadoId";

	@Inject
	private ManterRelatorioFormatadoService service;

	private List<RelatorioFormatado> rascunhos = new ArrayList<>();
	private List<RelatorioFormatado> rascunhosFiltrados = new ArrayList<>();
	private String termoBusca = "";

	@PostConstruct
	public void inicializar() {
		carregarRascunhos();
	}

	public void carregarRascunhos() {
		try {
			rascunhos = service.buscarTodos();
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar rascunhos formatados", e);
			rascunhos = new ArrayList<>();
			Mensagens.erro("Não foi possível carregar os rascunhos formatados.");
		}
		filtrar();
	}

	public void filtrar() {
		rascunhosFiltrados = FiltroRelatorios.formatados(rascunhos, termoBusca);
	}

	/** Botao "Limpar" da busca: apaga o termo e mostra todos. */
	public void limparBusca() {
		termoBusca = "";
		carregarRascunhos();
	}

	/** Guarda o ID na sessao e abre o editor com o rascunho. */
	public String abrirRascunho(Long rfId) {
		SessaoUsuario.guardar(CHAVE_RASCUNHO_A_ABRIR, rfId);
		return "/relatorio/VerificarRelatorio.xhtml?faces-redirect=true";
	}

	public void excluirRascunho(RelatorioFormatado rf) {
		try {
			service.excluir(rf);
			carregarRascunhos();
			Mensagens.sucesso("Rascunho formatado excluído.");
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao excluir rascunho formatado id={}", rf.getId(), e);
			Mensagens.erroInesperado();
		}
	}

	public List<RelatorioFormatado> getRascunhosFiltrados() {
		return rascunhosFiltrados;
	}

	public String getTermoBusca() {
		return termoBusca;
	}

	public void setTermoBusca(String t) {
		this.termoBusca = t;
	}
}

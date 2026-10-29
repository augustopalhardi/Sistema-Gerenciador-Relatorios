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

import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.service.FiltroRelatorios;
import com.augustopalhardi.relatorios.service.ManterRelatorioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;

/** Tela "Rascunhos de Relatorios": lista, busca, duplicacao e exclusao. */
@Named
@ViewScoped
public class RascunhosBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RascunhosBean.class);

	/** Chave de sessao com o ID que a tela de edicao deve abrir. */
	static final String CHAVE_RELATORIO_A_EDITAR = "relatorioEditandoId";

	@Inject
	private ManterRelatorioService relatorioService;

	private List<Relatorio> relatorios = new ArrayList<>();
	private List<Relatorio> relatoriosFiltrados = new ArrayList<>();
	private String termoBusca = "";

	@PostConstruct
	public void inicializar() {
		carregarRelatorios();
	}

	public void carregarRelatorios() {
		try {
			relatorios = relatorioService.buscarTodos();
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar relatórios", e);
			relatorios = new ArrayList<>();
			Mensagens.erro("Não foi possível carregar os relatórios.");
		}
		filtrar();
	}

	public void filtrar() {
		relatoriosFiltrados = FiltroRelatorios.relatorios(relatorios, termoBusca);
	}

	/** Botao "Limpar" da busca: apaga o termo e mostra todos. */
	public void limparBusca() {
		termoBusca = "";
		carregarRelatorios();
	}

	/** Guarda o ID na sessao e abre a tela de edicao. */
	public String abrirRelatorio(Long relatorioId) {
		SessaoUsuario.guardar(CHAVE_RELATORIO_A_EDITAR, relatorioId);
		return "/relatorio/Relatorio.xhtml?faces-redirect=true";
	}

	public void excluirRelatorio(Relatorio relatorio) {
		try {
			relatorioService.excluir(relatorio);
			carregarRelatorios();
			Mensagens.sucesso("Relatório excluído.");
		} catch (RegraNegocioException e) {
			Mensagens.aviso(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao excluir relatório id={}", relatorio.getId(), e);
			Mensagens.erroInesperado();
		}
	}

	public void duplicarRelatorio(Relatorio original) {
		try {
			relatorioService.duplicar(original, SessaoUsuario.nomeOuPadrao(null));
			carregarRelatorios();
			Mensagens.sucesso("Relatório duplicado. A cópia aparece na lista com \"(Cópia)\" no título.");
		} catch (RegraNegocioException e) {
			Mensagens.aviso(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao duplicar relatório id={}", original.getId(), e);
			Mensagens.erroInesperado();
		}
	}

	public List<Relatorio> getRelatoriosFiltrados() {
		return relatoriosFiltrados;
	}

	public String getTermoBusca() {
		return termoBusca;
	}

	public void setTermoBusca(String t) {
		this.termoBusca = t;
	}
}

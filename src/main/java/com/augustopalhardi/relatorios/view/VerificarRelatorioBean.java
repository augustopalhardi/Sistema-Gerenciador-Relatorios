package com.augustopalhardi.relatorios.view;

import java.io.Serializable;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.dto.ContatoEmpresa;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.RelatorioFormatado;
import com.augustopalhardi.relatorios.service.ManterDadosEmpresaService;
import com.augustopalhardi.relatorios.service.ManterRelatorioFormatadoService;
import com.augustopalhardi.relatorios.service.ManterRelatorioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.service.documento.GeradorDocumento;

/**
 * Tela "Formatar Relatorio": gera o documento diagramado a partir de um
 * relatorio e permite edita-lo no navegador e salva-lo como rascunho
 * formatado. A montagem do HTML fica no {@link GeradorDocumento}.
 */
@Named
@ViewScoped
public class VerificarRelatorioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(VerificarRelatorioBean.class);

	@Inject
	private ManterRelatorioService relatorioService;

	@Inject
	private ManterRelatorioFormatadoService formatadoService;

	@Inject
	private ManterDadosEmpresaService dadosEmpresaService;

	private List<Relatorio> relatorios = new ArrayList<>();
	private Long relatorioSelecionadoId;
	private Relatorio relatorioSelecionado;
	private String conteudoEditor = "";
	private boolean editorVisivel;

	/** Rascunho em edicao (null = documento novo, ainda nao salvo). */
	private Long rascunhoFormatadoId;

	@PostConstruct
	public void inicializar() {
		try {
			relatorios = relatorioService.buscarTodos();
			Long rfId = (Long) SessaoUsuario.retirar(RascunhosFormatadosBean.CHAVE_RASCUNHO_A_ABRIR);
			if (rfId != null) {
				carregarRascunhoFormatado(rfId);
			}
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao abrir a tela de formatação", e);
			Mensagens.erro("Não foi possível carregar a lista de relatórios.");
		}
	}

	public void formatarRelatorio() {
		if (relatorioSelecionadoId == null) {
			Mensagens.erro("Selecione um relatório.");
			return;
		}
		try {
			Relatorio rel = relatorioService.buscarPorId(relatorioSelecionadoId);
			if (rel == null) {
				Mensagens.erro("Relatório não encontrado. Ele pode ter sido excluído.");
				return;
			}
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
			conteudoEditor = GeradorDocumento.gerar(rel, relatorioService.buscarTopicosPorRelatorioId(rel.getId()),
					ContatoEmpresa.de(dadosEmpresaService.buscarDadosEmpresa()), contextPath, Year.now().getValue());
			relatorioSelecionado = rel;
			rascunhoFormatadoId = null;
			editorVisivel = true;
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao formatar relatório id={}", relatorioSelecionadoId, e);
			Mensagens.erroInesperado();
		}
	}

	public void fecharEditor() {
		editorVisivel = false;
		conteudoEditor = "";
		relatorioSelecionado = null;
		rascunhoFormatadoId = null;
	}

	public void salvarRascunho() {
		try {
			boolean novo = rascunhoFormatadoId == null;
			RelatorioFormatado rf = formatadoService.salvarRascunho(rascunhoFormatadoId, relatorioSelecionado,
					conteudoEditor, SessaoUsuario.nomeOuPadrao("sistema"));
			rascunhoFormatadoId = rf.getId();
			Mensagens.sucesso(novo ? "Rascunho formatado criado. Ele fica disponível em Rascunhos Formatados."
					: "Alterações do rascunho salvas.");
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar rascunho formatado", e);
			Mensagens.erroInesperado();
		}
	}

	private void carregarRascunhoFormatado(Long rfId) {
		RelatorioFormatado rf = formatadoService.buscarPorId(rfId);
		if (rf == null) {
			Mensagens.aviso("O rascunho escolhido não foi encontrado. Ele pode ter sido excluído.");
			return;
		}
		rascunhoFormatadoId = rf.getId();
		relatorioSelecionado = rf.getRelatorio();
		relatorioSelecionadoId = rf.getRelatorio() != null ? rf.getRelatorio().getId() : null;
		conteudoEditor = rf.getConteudoHtml();
		editorVisivel = true;
	}

	public List<Relatorio> getRelatorios() {
		return relatorios;
	}

	public Long getRelatorioSelecionadoId() {
		return relatorioSelecionadoId;
	}

	public void setRelatorioSelecionadoId(Long id) {
		this.relatorioSelecionadoId = id;
	}

	public String getConteudoEditor() {
		return conteudoEditor;
	}

	public void setConteudoEditor(String c) {
		this.conteudoEditor = c;
	}

	public boolean isEditorVisivel() {
		return editorVisivel;
	}
}

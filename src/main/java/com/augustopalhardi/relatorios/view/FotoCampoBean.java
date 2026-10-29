package com.augustopalhardi.relatorios.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.service.ManterFotoCampoService;
import com.augustopalhardi.relatorios.service.ManterRelatorioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Uploads;

/** Dialogo "Enviar Foto de Campo" da tela inicial. */
@Named
@ViewScoped
public class FotoCampoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(FotoCampoBean.class);

	@Inject
	private ManterFotoCampoService fotoCampoService;

	@Inject
	private ManterRelatorioService relatorioService;

	private UploadedFile fotoUpload;
	private String legendaFoto;
	private Long relatorioSelecionadoId;
	private List<Relatorio> relatoriosDisponiveis = new ArrayList<>();

	@PostConstruct
	public void inicializar() {
		try {
			relatoriosDisponiveis = relatorioService.buscarTodos();
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar relatórios para o envio de fotos", e);
			Mensagens.erro("Não foi possível carregar a lista de relatórios para o envio de fotos.");
		}
	}

	/** Grava a foto e a deixa pendente no relatorio escolhido. */
	public void uploadFotoCampo() {
		if (!ArquivosEnviados.selecionado(fotoUpload)) {
			Mensagens.erro("Selecione uma foto antes de enviar.");
			return;
		}
		if (relatorioSelecionadoId == null) {
			Mensagens.erro("Selecione o relatório ao qual esta foto pertence.");
			return;
		}
		try {
			Relatorio relatorio = relatorioService.buscarPorId(relatorioSelecionadoId);
			if (relatorio == null) {
				Mensagens.erro("Relatório não encontrado. Ele pode ter sido excluído.");
				return;
			}
			String nome = ArquivosEnviados.salvar(fotoUpload, Uploads.RELATORIOS, "campo", true,
					Uploads.EXTENSOES_FOTO);
			fotoCampoService.registrar(relatorio, SessaoUsuario.atual(), Uploads.url(Uploads.RELATORIOS, nome),
					legendaFoto);
			Mensagens.sucesso("Foto enviada. Ela aparecerá no painel \"Fotos do Campo\" ao editar o relatório.");
			fotoUpload = null;
			legendaFoto = null;
			relatorioSelecionadoId = null;
			PrimeFaces.current().ajax().addCallbackParam("salvo", true);
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Erro ao salvar foto de campo", e);
			Mensagens.erroInesperado();
		}
	}

	public UploadedFile getFotoUpload() {
		return fotoUpload;
	}

	public void setFotoUpload(UploadedFile fotoUpload) {
		this.fotoUpload = fotoUpload;
	}

	public String getLegendaFoto() {
		return legendaFoto;
	}

	public void setLegendaFoto(String legendaFoto) {
		this.legendaFoto = legendaFoto;
	}

	public Long getRelatorioSelecionadoId() {
		return relatorioSelecionadoId;
	}

	public void setRelatorioSelecionadoId(Long relatorioSelecionadoId) {
		this.relatorioSelecionadoId = relatorioSelecionadoId;
	}

	public List<Relatorio> getRelatoriosDisponiveis() {
		return relatoriosDisponiveis;
	}
}

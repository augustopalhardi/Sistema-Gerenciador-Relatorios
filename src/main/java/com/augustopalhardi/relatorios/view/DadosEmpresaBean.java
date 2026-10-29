package com.augustopalhardi.relatorios.view;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.model.DadosEmpresa;
import com.augustopalhardi.relatorios.service.ManterDadosEmpresaService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Uploads;

/** Tela "Minha Empresa": dados da empresa prestadora usados no documento. */
@Named
@ViewScoped
public class DadosEmpresaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DadosEmpresaBean.class);

	@Inject
	private ManterDadosEmpresaService service;

	private DadosEmpresa dados = new DadosEmpresa();
	private UploadedFile uploadedLogo;

	@PostConstruct
	public void inicializar() {
		try {
			dados = service.buscarDadosEmpresa();
			if (dados.getId() == null) {
				Mensagens.aviso("Os dados da empresa ainda não foram configurados. Os valores abaixo são exemplos.");
			}
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar dados da empresa", e);
			Mensagens.erro("Não foi possível carregar os dados da empresa.");
		}
	}

	public void salvar() {
		try {
			String falhaLogo = null;
			String logoAntigo = dados.getUrlLogo();
			if (ArquivosEnviados.selecionado(uploadedLogo)) {
				try {
					dados.setUrlLogo(ArquivosEnviados.salvar(uploadedLogo, Uploads.EMPRESA, "logo_empresa", false,
							Uploads.EXTENSOES_LOGO));
				} catch (RegraNegocioException e) {
					falhaLogo = e.getMessage();
				} catch (IOException e) {
					LOGGER.error("Erro ao gravar logo da empresa", e);
					falhaLogo = "o arquivo não pôde ser gravado";
				}
			}

			dados = service.salvar(dados);
			uploadedLogo = null;
			if (logoAntigo != null && !logoAntigo.equals(dados.getUrlLogo())) {
				Uploads.excluir(Uploads.EMPRESA, logoAntigo);
			}

			if (falhaLogo == null) {
				Mensagens.sucesso("Dados da empresa salvos.");
			} else {
				Mensagens.aviso("Dados salvos, mas o logo não foi atualizado: " + falhaLogo);
			}
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar dados da empresa", e);
			Mensagens.erroInesperado();
		}
	}

	public void removerLogo() {
		if (!isLogoExistente()) {
			Mensagens.aviso("Não há logo cadastrado para remover.");
			return;
		}
		try {
			dados = service.removerLogo(dados);
			Mensagens.sucesso("Logo removido.");
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao remover logo da empresa", e);
			Mensagens.erroInesperado();
		}
	}

	public boolean isLogoExistente() {
		return dados != null && dados.getUrlLogo() != null && !dados.getUrlLogo().isEmpty();
	}

	public DadosEmpresa getDados() {
		return dados;
	}

	public void setDados(DadosEmpresa dados) {
		this.dados = dados;
	}

	public UploadedFile getUploadedLogo() {
		return uploadedLogo;
	}

	public void setUploadedLogo(UploadedFile uploadedLogo) {
		this.uploadedLogo = uploadedLogo;
	}
}

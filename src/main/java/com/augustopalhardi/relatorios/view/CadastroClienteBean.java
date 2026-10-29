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

import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.service.ManterCadastroClienteService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;

/** Tela "Cadastro de Clientes": formulario de novo cliente, lista e dialogo de edicao. */
@Named
@ViewScoped
public class CadastroClienteBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CadastroClienteBean.class);

	@Inject
	private ManterCadastroClienteService clienteService;

	private List<CadastroCliente> cadastroClientes = new ArrayList<>();
	private UploadedFile uploadedLogo;

	private String nomeEmpresa;
	private String localEmpresa;
	private String cnpj;
	private String responsavel;
	private String telefone;
	private String email;
	private String observacoes;

	/**
	 * ID do cliente em edicao (null = novo). Trafega num campo oculto do dialogo
	 * para que o salvar() saiba que e uma alteracao.
	 */
	private Long clienteEditandoId;

	@PostConstruct
	public void inicializar() {
		limpar();
		carregarClientes();
	}

	private void carregarClientes() {
		try {
			cadastroClientes = clienteService.buscarTodos();
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar clientes", e);
			cadastroClientes = new ArrayList<>();
			Mensagens.erro("Não foi possível carregar a lista de clientes.");
		}
	}

	public void limpar() {
		nomeEmpresa = null;
		localEmpresa = null;
		cnpj = null;
		responsavel = null;
		telefone = null;
		email = null;
		observacoes = null;
		uploadedLogo = null;
		clienteEditandoId = null;
	}

	/** Preenche o dialogo de edicao com os dados do cliente. */
	public void prepararEdicao(CadastroCliente cliente) {
		clienteEditandoId = cliente.getId();
		nomeEmpresa = cliente.getNomeEmpresa();
		localEmpresa = cliente.getLocalEmpresa();
		cnpj = cliente.getCnpj();
		responsavel = cliente.getResponsavel();
		telefone = cliente.getTelefone();
		email = cliente.getEmail();
		observacoes = cliente.getObservacoes();
		uploadedLogo = null;
	}

	/** Botao do formulario principal: sempre cadastra um cliente novo. */
	public void salvarNovo() {
		clienteEditandoId = null;
		salvar();
	}

	/** Botao do dialogo de edicao (ou do formulario, se nao houver edicao em andamento). */
	public void salvar() {
		boolean edicao = clienteEditandoId != null;
		try {
			CadastroCliente cliente = edicao ? clienteService.buscarPorId(clienteEditandoId) : new CadastroCliente();
			if (cliente == null) {
				Mensagens.erro("Cliente não encontrado. Ele pode ter sido excluído por outro usuário.");
				return;
			}
			cliente.setNomeEmpresa(Texto.aparar(nomeEmpresa));
			cliente.setLocalEmpresa(Texto.nvl(localEmpresa));
			cliente.setCnpj(Texto.aparar(cnpj));
			cliente.setResponsavel(Texto.nvl(responsavel));
			cliente.setTelefone(Texto.nvl(telefone));
			cliente.setEmail(Texto.nvl(email));
			cliente.setObservacoes(Texto.aparar(observacoes));

			String falhaLogo = null;
			String logoAntigo = cliente.getUrlLogo();
			if (ArquivosEnviados.selecionado(uploadedLogo)) {
				try {
					cliente.setUrlLogo(ArquivosEnviados.salvar(uploadedLogo, Uploads.CLIENTES, "cliente", false,
							Uploads.EXTENSOES_LOGO));
				} catch (RegraNegocioException e) {
					falhaLogo = e.getMessage();
				} catch (IOException e) {
					LOGGER.error("Erro ao gravar logo do cliente", e);
					falhaLogo = "o arquivo não pôde ser gravado";
				}
			}

			clienteService.salvar(cliente);
			if (logoAntigo != null && !logoAntigo.equals(cliente.getUrlLogo())) {
				Uploads.excluir(Uploads.CLIENTES, logoAntigo);
			}

			String acao = edicao ? "atualizado" : "cadastrado";
			if (falhaLogo == null) {
				Mensagens.sucesso("Cliente " + acao + ".");
			} else {
				Mensagens.aviso("Cliente " + acao + ", mas o logo não foi salvo: " + falhaLogo);
			}
			limpar();
			carregarClientes();
			PrimeFaces.current().ajax().addCallbackParam("salvo", true);
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar cliente", e);
			Mensagens.erroInesperado();
		}
	}

	public void excluir(CadastroCliente cliente) {
		try {
			clienteService.excluir(cliente);
			cadastroClientes.remove(cliente);
			Mensagens.sucesso("Cliente \"" + cliente.getNomeEmpresa() + "\" excluído.");
		} catch (RegraNegocioException e) {
			Mensagens.aviso(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao excluir cliente id={}", cliente.getId(), e);
			Mensagens.erroInesperado();
		}
	}

	public boolean isEditando() {
		return clienteEditandoId != null;
	}

	public List<CadastroCliente> getCadastroClientes() {
		return cadastroClientes;
	}

	public UploadedFile getUploadedLogo() {
		return uploadedLogo;
	}

	public void setUploadedLogo(UploadedFile f) {
		this.uploadedLogo = f;
	}

	public String getNomeEmpresa() {
		return nomeEmpresa;
	}

	public void setNomeEmpresa(String v) {
		this.nomeEmpresa = v;
	}

	public String getLocalEmpresa() {
		return localEmpresa;
	}

	public void setLocalEmpresa(String v) {
		this.localEmpresa = v;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String v) {
		this.cnpj = v;
	}

	public String getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(String v) {
		this.responsavel = v;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String v) {
		this.telefone = v;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		this.email = v;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String v) {
		this.observacoes = v;
	}

	public Long getClienteEditandoId() {
		return clienteEditandoId;
	}

	public void setClienteEditandoId(Long v) {
		this.clienteEditandoId = v;
	}
}

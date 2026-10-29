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

import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.service.ManterCadastroUsuarioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;

/** Tela "Minha Conta": dados pessoais, troca de senha e foto de perfil. */
@Named
@ViewScoped
public class MinhaContaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MinhaContaBean.class);

	@Inject
	private ManterCadastroUsuarioService usuarioService;

	private CadastroUsuario usuarioLogado;

	private String nome;
	private String email;
	private String funcao;

	private String senhaAtual;
	private String novaSenha;
	private String confirmarNovaSenha;

	private UploadedFile fotoUpload;

	@PostConstruct
	public void inicializar() {
		try {
			CadastroUsuario daSessao = SessaoUsuario.atual();
			usuarioLogado = daSessao != null ? usuarioService.buscarPorId(daSessao.getId()) : null;
			if (usuarioLogado == null) {
				Mensagens.erro("Não foi possível carregar sua conta. Saia e entre novamente.");
				usuarioLogado = new CadastroUsuario();
				return;
			}
			copiarParaFormulario(usuarioLogado);
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar a conta do usuário", e);
			usuarioLogado = new CadastroUsuario();
			Mensagens.erroInesperado();
		}
	}

	public void salvarDadosPessoais() {
		if (!contaCarregada()) {
			return;
		}
		try {
			usuarioLogado.setNome(nome);
			usuarioLogado.setEmail(email);
			usuarioLogado.setFuncao(funcao);
			atualizarSessao(usuarioService.atualizar(usuarioLogado, null));
			Mensagens.sucesso("Dados pessoais atualizados.");
		} catch (RegraNegocioException e) {
			recarregar();
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar dados pessoais", e);
			recarregar();
			Mensagens.erroInesperado();
		}
	}

	public void alterarSenha() {
		if (!contaCarregada()) {
			return;
		}
		try {
			atualizarSessao(usuarioService.alterarSenha(usuarioLogado, senhaAtual, novaSenha, confirmarNovaSenha));
			Mensagens.sucesso("Senha alterada.");
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao alterar senha", e);
			Mensagens.erroInesperado();
		} finally {
			senhaAtual = null;
			novaSenha = null;
			confirmarNovaSenha = null;
		}
	}

	public void uploadFoto() {
		if (!contaCarregada()) {
			return;
		}
		if (!ArquivosEnviados.selecionado(fotoUpload)) {
			Mensagens.erro("Selecione uma imagem para enviar.");
			return;
		}
		try {
			String fotoAntiga = usuarioLogado.getUrlFoto();
			String nomeArquivo = ArquivosEnviados.salvar(fotoUpload, Uploads.USUARIOS,
					"usuario_" + usuarioLogado.getId(), false, Uploads.EXTENSOES_FOTO);
			usuarioLogado.setUrlFoto(Uploads.url(Uploads.USUARIOS, nomeArquivo));
			atualizarSessao(usuarioService.salvar(usuarioLogado));
			Uploads.excluirPorUrl(fotoAntiga);
			fotoUpload = null;
			Mensagens.sucesso("Foto de perfil atualizada.");
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Erro no upload da foto de perfil", e);
			recarregar();
			Mensagens.erroInesperado();
		}
	}

	public void removerFoto() {
		if (!contaCarregada()) {
			return;
		}
		if (Texto.vazio(usuarioLogado.getUrlFoto())) {
			Mensagens.aviso("Você não tem foto de perfil para remover.");
			return;
		}
		try {
			String foto = usuarioLogado.getUrlFoto();
			usuarioLogado.setUrlFoto(null);
			atualizarSessao(usuarioService.salvar(usuarioLogado));
			Uploads.excluirPorUrl(foto);
			Mensagens.sucesso("Foto de perfil removida.");
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao remover foto de perfil", e);
			recarregar();
			Mensagens.erroInesperado();
		}
	}

	private boolean contaCarregada() {
		if (usuarioLogado == null || usuarioLogado.getId() == null) {
			Mensagens.erro("Sua conta não está carregada. Saia e entre novamente.");
			return false;
		}
		return true;
	}

	/** Descarta alteracoes em memoria e volta ao que esta gravado. */
	private void recarregar() {
		CadastroUsuario gravado = usuarioService.buscarPorId(usuarioLogado.getId());
		if (gravado != null) {
			usuarioLogado = gravado;
		}
	}

	private void atualizarSessao(CadastroUsuario atualizado) {
		SessaoUsuario.definir(atualizado);
		usuarioLogado = atualizado;
		copiarParaFormulario(atualizado);
	}

	private void copiarParaFormulario(CadastroUsuario u) {
		nome = u.getNome();
		email = u.getEmail();
		funcao = u.getFuncao();
	}

	public CadastroUsuario getUsuarioLogado() {
		return usuarioLogado;
	}

	/** Inicial do nome para o avatar sem foto. */
	public String getInicialNome() {
		return Texto.inicial(usuarioLogado != null ? usuarioLogado.getNome() : null);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFuncao() {
		return funcao;
	}

	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String s) {
		this.senhaAtual = s;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String s) {
		this.novaSenha = s;
	}

	public String getConfirmarNovaSenha() {
		return confirmarNovaSenha;
	}

	public void setConfirmarNovaSenha(String s) {
		this.confirmarNovaSenha = s;
	}

	public UploadedFile getFotoUpload() {
		return fotoUpload;
	}

	public void setFotoUpload(UploadedFile f) {
		this.fotoUpload = f;
	}
}

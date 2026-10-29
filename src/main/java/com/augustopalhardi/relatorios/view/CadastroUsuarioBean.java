package com.augustopalhardi.relatorios.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.service.ManterCadastroUsuarioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;

/**
 * Tela "Gerenciar Usuarios" e dialogo "Criar Primeira Conta" da tela de login.
 * As regras (validacao, hash de senha, bloqueios de exclusao) ficam no
 * {@link ManterCadastroUsuarioService}.
 */
@Named
@ViewScoped
public class CadastroUsuarioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CadastroUsuarioBean.class);

	@Inject
	private ManterCadastroUsuarioService usuarioService;

	private List<CadastroUsuario> cadastroUsuarios = new ArrayList<>();

	private String nome;
	private String email;
	private String senha;
	private String funcao;

	/** Usuario em edicao no dialogo (null = cadastro novo). */
	private CadastroUsuario usuarioSelecionado;

	@PostConstruct
	public void inicializar() {
		limpar();
		if (SessaoUsuario.atual() != null) {
			carregarUsuarios();
		}
	}

	private void carregarUsuarios() {
		try {
			cadastroUsuarios = usuarioService.buscarTodos();
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar usuários", e);
			cadastroUsuarios = new ArrayList<>();
			Mensagens.erro("Não foi possível carregar a lista de usuários.");
		}
	}

	public void limpar() {
		nome = "";
		email = "";
		senha = "";
		funcao = "";
		usuarioSelecionado = null;
	}

	/** Preenche o dialogo de edicao com os dados do usuario. */
	public void prepararEdicao(CadastroUsuario usuario) {
		usuarioSelecionado = usuario;
		nome = usuario.getNome();
		email = usuario.getEmail();
		senha = "";
		funcao = usuario.getFuncao();
	}

	/** Botao do formulario principal: sempre cadastra um usuario novo. */
	public void salvarNovo() {
		usuarioSelecionado = null;
		salvar();
	}

	/** Botao do dialogo de edicao. */
	public void salvar() {
		boolean edicao = usuarioSelecionado != null;
		try {
			CadastroUsuario usuario = edicao ? usuarioSelecionado : new CadastroUsuario();
			usuario.setNome(nome);
			usuario.setEmail(email);
			usuario.setFuncao(funcao);
			if (edicao) {
				usuarioService.atualizar(usuario, senha);
				atualizarSessaoSeForOProprio(usuario);
			} else {
				usuarioService.cadastrar(usuario, senha);
			}
			Mensagens.sucesso(edicao ? "Usuário atualizado." : "Usuário cadastrado.");
			limpar();
			carregarUsuarios();
			PrimeFaces.current().ajax().addCallbackParam("salvo", true);
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
			if (edicao) {
				// desfaz as alteracoes em memoria do usuario da lista
				carregarUsuarios();
				usuarioSelecionado = buscarNaLista(usuarioSelecionado.getId());
			}
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar usuário", e);
			Mensagens.erroInesperado();
		}
	}

	/** Dialogo da tela de login: cria o administrador inicial. */
	public void salvarPrimeiraConta() {
		try {
			usuarioService.criarPrimeiraConta(nome, email, senha, funcao);
			Mensagens.sucesso("Conta de administrador criada. Entre com o e-mail e a senha cadastrados.");
			limpar();
			PrimeFaces.current().ajax().addCallbackParam("salvo", true);
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao criar primeira conta", e);
			Mensagens.erroInesperado();
		}
	}

	public void excluir(CadastroUsuario usuario) {
		try {
			CadastroUsuario logado = SessaoUsuario.atual();
			usuarioService.excluir(usuario, logado != null ? logado.getId() : null);
			cadastroUsuarios.remove(usuario);
			Mensagens.sucesso("Usuário \"" + usuario.getNome() + "\" excluído.");
		} catch (RegraNegocioException e) {
			Mensagens.aviso(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao excluir usuário id={}", usuario.getId(), e);
			Mensagens.erroInesperado();
		}
	}

	private void atualizarSessaoSeForOProprio(CadastroUsuario usuario) {
		CadastroUsuario logado = SessaoUsuario.atual();
		if (logado != null && logado.getId().equals(usuario.getId())) {
			SessaoUsuario.definir(usuarioService.buscarPorId(usuario.getId()));
		}
	}

	private CadastroUsuario buscarNaLista(Long id) {
		return cadastroUsuarios.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
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

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getFuncao() {
		return funcao;
	}

	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

	public List<CadastroUsuario> getCadastroUsuarios() {
		return cadastroUsuarios;
	}

	public CadastroUsuario getUsuarioSelecionado() {
		return usuarioSelecionado;
	}
}

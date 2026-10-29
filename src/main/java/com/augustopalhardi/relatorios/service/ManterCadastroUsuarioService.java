package com.augustopalhardi.relatorios.service;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.CadastroUsuarioDao;
import com.augustopalhardi.relatorios.dao.FotoCampoDao;
import com.augustopalhardi.relatorios.dao.RelatorioDao;
import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.model.TipoUsuario;
import com.augustopalhardi.relatorios.util.Senhas;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

/**
 * Regras de cadastro de usuarios. Toda senha recebida aqui e texto puro; o hash
 * e gerado so neste servico, nunca nas telas.
 */
@ApplicationScoped
public class ManterCadastroUsuarioService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroUsuarioDao cadastroUsuarioDao;

	@Inject
	private RelatorioDao relatorioDao;

	@Inject
	private FotoCampoDao fotoCampoDao;

	public List<CadastroUsuario> buscarTodos() {
		return cadastroUsuarioDao.buscarTodos();
	}

	public CadastroUsuario buscarPorId(Long usuarioId) {
		return cadastroUsuarioDao.buscarPeloCodigo(usuarioId);
	}

	public boolean existeAlgumUsuario() {
		return cadastroUsuarioDao.contarUsuarios() > 0;
	}

	/**
	 * Cria a primeira conta do sistema (administrador). So e permitido enquanto
	 * nao houver nenhum usuario cadastrado.
	 */
	@Transactional
	public CadastroUsuario criarPrimeiraConta(String nome, String email, String senha, String funcao) {
		if (existeAlgumUsuario()) {
			throw new RegraNegocioException("Já existe um usuário cadastrado. Faça login para continuar.");
		}
		CadastroUsuario usuario = new CadastroUsuario();
		usuario.setNome(nome);
		usuario.setEmail(email);
		usuario.setFuncao(Texto.vazio(funcao) ? "Administrador" : funcao);
		return cadastrar(usuario, senha);
	}

	/** Cadastra um novo usuario com a senha informada (texto puro). */
	@Transactional
	public CadastroUsuario cadastrar(CadastroUsuario usuario, String senha) {
		validarDados(usuario);
		RegraNegocioException.seHouver(Senhas.validarNova(senha, null));
		usuario.setSenha(Senhas.gerarHash(senha));
		usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
		return cadastroUsuarioDao.salvar(usuario);
	}

	/**
	 * Atualiza nome, e-mail e funcao. Se novaSenha vier preenchida, troca a senha
	 * tambem; vazia ou null mantem a atual.
	 */
	@Transactional
	public CadastroUsuario atualizar(CadastroUsuario usuario, String novaSenha) {
		validarDados(usuario);
		if (novaSenha != null && !novaSenha.isEmpty()) {
			RegraNegocioException.seHouver(Senhas.validarNova(novaSenha, null));
			usuario.setSenha(Senhas.gerarHash(novaSenha));
		}
		return cadastroUsuarioDao.salvar(usuario);
	}

	/** Troca de senha pelo proprio usuario, exigindo a senha atual. */
	@Transactional
	public CadastroUsuario alterarSenha(CadastroUsuario usuario, String senhaAtual, String novaSenha,
			String confirmacao) {
		if (senhaAtual == null || senhaAtual.isEmpty()) {
			throw new RegraNegocioException("Informe sua senha atual.");
		}
		RegraNegocioException.seHouver(Senhas.validarNova(novaSenha, confirmacao));
		if (!Senhas.confere(senhaAtual, usuario.getSenha())) {
			throw new RegraNegocioException("Senha atual incorreta.");
		}
		usuario.setSenha(Senhas.gerarHash(novaSenha));
		return cadastroUsuarioDao.salvar(usuario);
	}

	/** Grava alteracoes que nao envolvem senha nem dados validados (ex.: foto). */
	@Transactional
	public CadastroUsuario salvar(CadastroUsuario usuario) {
		return cadastroUsuarioDao.salvar(usuario);
	}

	/**
	 * Exclui um usuario. Bloqueia a exclusao da propria conta e de usuarios com
	 * relatorios ou fotos de campo vinculados.
	 */
	@Transactional
	public void excluir(CadastroUsuario usuario, Long idUsuarioLogado) {
		if (usuario.getId() != null && usuario.getId().equals(idUsuarioLogado)) {
			throw new RegraNegocioException("Você não pode excluir a própria conta enquanto está conectado com ela.");
		}
		long relatorios = relatorioDao.contarPorUsuarioRegistro(usuario.getNome());
		long fotos = fotoCampoDao.contarPorUsuario(usuario.getId());
		RegraNegocioException.seHouver(motivoBloqueioExclusao(usuario.getNome(), relatorios, fotos));
		cadastroUsuarioDao.excluir(usuario);
	}

	/**
	 * Mensagem explicando por que o usuario nao pode ser excluido, ou null se
	 * puder. Funcao pura, separada para teste.
	 */
	static String motivoBloqueioExclusao(String nome, long relatorios, long fotos) {
		if (relatorios == 0 && fotos == 0) {
			return null;
		}
		StringBuilder vinculos = new StringBuilder();
		if (relatorios > 0) {
			vinculos.append(relatorios).append(" relatório(s)");
		}
		if (fotos > 0) {
			if (vinculos.length() > 0) {
				vinculos.append(" e ");
			}
			vinculos.append(fotos).append(" foto(s) de campo");
		}
		return "Não é possível excluir o usuário \"" + nome + "\": ele está vinculado a " + vinculos
				+ ". Exclua ou transfira esses registros antes.";
	}

	/** Valida nome e e-mail e garante que o e-mail nao pertence a outro usuario. */
	private void validarDados(CadastroUsuario usuario) {
		usuario.setNome(Texto.aparar(usuario.getNome()));
		usuario.setEmail(Texto.aparar(usuario.getEmail()));
		usuario.setFuncao(Texto.nvl(usuario.getFuncao()));
		RegraNegocioException.seHouver(validarNomeEmail(usuario.getNome(), usuario.getEmail()));
		CadastroUsuario comMesmoEmail = cadastroUsuarioDao.buscarPorEmail(usuario.getEmail());
		if (comMesmoEmail != null && !comMesmoEmail.getId().equals(usuario.getId())) {
			throw new RegraNegocioException("Este e-mail já está cadastrado para outro usuário.");
		}
	}

	/** Mensagem de erro para nome/e-mail invalidos, ou null. Funcao pura. */
	static String validarNomeEmail(String nome, String email) {
		if (Texto.vazio(nome)) {
			return "Nome é obrigatório.";
		}
		if (Texto.vazio(email)) {
			return "E-mail é obrigatório.";
		}
		if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			return "Informe um e-mail válido.";
		}
		return null;
	}
}

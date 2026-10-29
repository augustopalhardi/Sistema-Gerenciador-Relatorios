package com.augustopalhardi.relatorios.service;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.dao.CadastroUsuarioDao;
import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.util.Senhas;
import com.augustopalhardi.relatorios.util.Texto;

@RequestScoped
public class LoginService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

	@Inject
	private CadastroUsuarioDao cadastroUsuarioDao;

	/**
	 * Devolve o usuario se o e-mail existir e a senha conferir com o hash BCrypt
	 * gravado; caso contrario devolve null. Nao diferencia "e-mail inexistente" de
	 * "senha errada", para nao revelar quais e-mails estao cadastrados.
	 */
	public CadastroUsuario autenticar(String email, String senha) {
		if (Texto.vazio(email) || senha == null || senha.isEmpty()) {
			return null;
		}
		CadastroUsuario usuario = cadastroUsuarioDao.buscarPorEmail(email.trim());
		if (usuario != null && Senhas.confere(senha, usuario.getSenha())) {
			LOGGER.info("Login realizado: usuario id={}", usuario.getId());
			return usuario;
		}
		LOGGER.info("Tentativa de login recusada");
		return null;
	}
}

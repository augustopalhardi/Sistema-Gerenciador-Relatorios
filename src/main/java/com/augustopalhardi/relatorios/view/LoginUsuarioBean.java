package com.augustopalhardi.relatorios.view;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.filter.RegrasAcesso;
import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.service.LoginService;
import com.augustopalhardi.relatorios.service.ManterCadastroUsuarioService;

/**
 * Login e logout. O usuario autenticado fica na sessao (ver
 * {@link SessaoUsuario}); este bean so vive durante a requisicao, para que a
 * senha digitada nunca fique guardada na sessao.
 */
@Named
@RequestScoped
public class LoginUsuarioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginUsuarioBean.class);

	@Inject
	private LoginService loginService;

	@Inject
	private ManterCadastroUsuarioService usuarioService;

	private String emailInput;
	private String senhaInput;
	private Boolean semUsuarios;

	public void autenticar() {
		try {
			CadastroUsuario usuario = loginService.autenticar(emailInput, senhaInput);
			senhaInput = null;
			if (usuario == null) {
				Mensagens.erro("E-mail ou senha incorretos. Verifique seus dados e tente novamente.");
				return;
			}
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			// novo ID de sessao no login, contra fixacao de sessao
			ec.getSession(true);
			((HttpServletRequest) ec.getRequest()).changeSessionId();
			SessaoUsuario.definir(usuario);
			ec.redirect(ec.getRequestContextPath() + RegrasAcesso.PAGINA_INICIAL);
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Erro durante o login", e);
			Mensagens.erroInesperado();
		}
	}

	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return RegrasAcesso.PAGINA_LOGIN + "?faces-redirect=true";
	}

	/**
	 * Chamado ao abrir a tela de login: explica por que o usuario voltou para
	 * ela quando a sessao expirou.
	 */
	public void avisarSeSessaoExpirou() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (!fc.isPostback() && "expirada".equals(fc.getExternalContext().getRequestParameterMap().get("sessao"))) {
			Mensagens.aviso("Sua sessão expirou. Entre novamente para continuar.");
		}
	}

	/** True enquanto nenhum usuario foi cadastrado (libera "Criar conta"). */
	public boolean isSemUsuarios() {
		if (semUsuarios == null) {
			try {
				semUsuarios = !usuarioService.existeAlgumUsuario();
			} catch (RuntimeException e) {
				LOGGER.error("Erro ao verificar se existem usuários", e);
				semUsuarios = false;
			}
		}
		return semUsuarios;
	}

	public String getEmailInput() {
		return emailInput;
	}

	public void setEmailInput(String emailInput) {
		this.emailInput = emailInput;
	}

	public String getSenhaInput() {
		return senhaInput;
	}

	public void setSenhaInput(String senhaInput) {
		this.senhaInput = senhaInput;
	}
}

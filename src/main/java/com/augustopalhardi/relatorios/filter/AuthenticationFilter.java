package com.augustopalhardi.relatorios.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.augustopalhardi.relatorios.view.SessaoUsuario;

/**
 * Autenticacao das telas: sem usuario na sessao, so as paginas publicas
 * abrem; as demais redirecionam para o login. Usuario logado so abre as telas
 * listadas em {@link RegrasAcesso}.
 *
 * Em requisicoes AJAX do JSF o redirect e enviado como partial-response, para
 * que o navegador realmente troque de pagina em vez de falhar em silencio.
 */
@WebFilter(urlPatterns = { "*.xhtml" })
public class AuthenticationFilter implements Filter {

	/** Parametro que a tela de login usa para explicar o redirecionamento. */
	public static final String PARAM_SESSAO_EXPIRADA = "sessao=expirada";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String caminho = req.getServletPath();
		String contexto = req.getContextPath();

		HttpSession sessao = req.getSession(false);
		boolean logado = sessao != null && sessao.getAttribute(SessaoUsuario.ATRIBUTO) != null;

		if (!caminho.startsWith("/javax.faces.resource/")) {
			// telas nunca vao para o cache: evita voltar a uma tela logada apos o logout
			res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			res.setHeader("Pragma", "no-cache");
			res.setDateHeader("Expires", 0);
		}

		if (RegrasAcesso.ehPublico(caminho)) {
			if (logado && RegrasAcesso.ehLogin(caminho)) {
				redirecionar(req, res, contexto + RegrasAcesso.PAGINA_INICIAL);
			} else {
				chain.doFilter(request, response);
			}
			return;
		}

		if (!logado) {
			boolean expirou = req.getRequestedSessionId() != null && !req.isRequestedSessionIdValid();
			redirecionar(req, res,
					contexto + RegrasAcesso.PAGINA_LOGIN + (expirou ? "?" + PARAM_SESSAO_EXPIRADA : ""));
			return;
		}

		if (RegrasAcesso.ehPermitido(caminho)) {
			chain.doFilter(request, response);
		} else {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void redirecionar(HttpServletRequest req, HttpServletResponse res, String url) throws IOException {
		if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
			res.setContentType("text/xml");
			res.setCharacterEncoding("UTF-8");
			res.getWriter().printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<partial-response><redirect url=\"%s\"></redirect></partial-response>", url);
		} else {
			res.sendRedirect(url);
		}
	}
}

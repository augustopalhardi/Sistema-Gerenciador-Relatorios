package com.augustopalhardi.relatorios.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.util.Uploads;
import com.augustopalhardi.relatorios.view.SessaoUsuario;

/**
 * Entrega os arquivos enviados pelos usuarios (logos e fotos), que ficam fora
 * do .war, no diretorio de uploads. So responde a usuarios autenticados e so
 * le arquivos dentro do diretorio de uploads.
 */
@WebServlet("/uploads/*")
public class ImageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);

	@Override
	public void init() {
		File base = Uploads.diretorioBase().toFile();
		if (!base.exists() && !base.mkdirs()) {
			LOGGER.error("Não foi possível criar o diretório de uploads: {}", base);
		} else {
			LOGGER.info("Diretório de uploads: {}", base);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession sessao = request.getSession(false);
		if (sessao == null || sessao.getAttribute(SessaoUsuario.ATRIBUTO) == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String caminho = request.getPathInfo();
		File arquivo = caminho != null && caminho.length() > 1 ? Uploads.resolverDentroDoBase(caminho.substring(1))
				: null;
		if (arquivo == null) {
			LOGGER.warn("Caminho de upload recusado: {}", caminho);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (!arquivo.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String tipo = Uploads.tipoConteudo(arquivo.getName());
		String disposicao = Uploads.exibivelNoNavegador(tipo) ? "inline" : "attachment";
		response.setContentType(tipo);
		response.setContentLengthLong(arquivo.length());
		response.setHeader("Cache-Control", "private, max-age=3600");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Content-Disposition", disposicao + "; filename=\"" + arquivo.getName() + "\"");
		Files.copy(arquivo.toPath(), response.getOutputStream());
	}
}

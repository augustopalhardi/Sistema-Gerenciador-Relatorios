package com.augustopalhardi.relatorios.view;

import java.io.IOException;
import java.util.Set;

import org.primefaces.model.file.UploadedFile;

import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Uploads;

/**
 * Ponte entre o upload do PrimeFaces e o {@link Uploads}: valida o arquivo e
 * grava em disco com nome unico. Usado por todas as telas que recebem arquivo.
 */
public final class ArquivosEnviados {

	private ArquivosEnviados() {
	}

	/** Indica se o usuario selecionou algum arquivo. */
	public static boolean selecionado(UploadedFile arquivo) {
		return arquivo != null && arquivo.getFileName() != null && !arquivo.getFileName().trim().isEmpty();
	}

	/**
	 * Valida e grava o arquivo. Devolve so o nome gravado (sem a pasta).
	 *
	 * @throws RegraNegocioException se o arquivo for recusado (formato ou tamanho)
	 * @throws IOException           se a gravacao em disco falhar
	 */
	public static String salvar(UploadedFile arquivo, String subpasta, String prefixo, boolean manterNomeOriginal,
			Set<String> extensoesAceitas) throws IOException {
		RegraNegocioException.seHouver(Uploads.validar(arquivo.getFileName(), arquivo.getSize(), extensoesAceitas));
		String nome = Uploads.gerarNome(prefixo, arquivo.getFileName(), manterNomeOriginal, System.currentTimeMillis());
		Uploads.salvar(arquivo.getInputStream(), subpasta, nome);
		return nome;
	}
}

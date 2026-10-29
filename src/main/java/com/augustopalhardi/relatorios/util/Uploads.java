package com.augustopalhardi.relatorios.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Centraliza onde os arquivos enviados pelos usuarios ficam em disco, como sao
 * nomeados, validados e expostos por URL.
 *
 * Diretorio base: chave "upload.dir" do arquivo de configuracao externo (ver
 * {@link Config}). Sem a chave, usa ~/relatorios-uploads.
 *
 * Cada tipo de arquivo fica numa subpasta, e a URL publica correspondente e
 * /uploads/{subpasta}/{arquivo}, servida pelo ImageServlet.
 */
public final class Uploads {

	public static final String PREFIXO_URL = "/uploads/";

	public static final String CLIENTES = "clientes";
	public static final String RELATORIOS = "relatorios";
	public static final String EMPRESA = "empresa";
	public static final String USUARIOS = "usuarios";

	/** Tamanho maximo aceito por arquivo (o mesmo limite informado nas telas). */
	public static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;

	/** Extensoes aceitas para fotos (relatorio, campo, perfil). */
	public static final Set<String> EXTENSOES_FOTO = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp")));

	/** Extensoes aceitas para logos (inclui SVG). */
	public static final Set<String> EXTENSOES_LOGO = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "svg")));

	/** Tipos que o navegador pode exibir direto; os demais sao baixados. */
	private static final Set<String> TIPOS_EXIBIVEIS = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp")));

	private Uploads() {
	}

	// ---------------------------------------------------------------
	// Diretorios e URLs
	// ---------------------------------------------------------------

	/** Diretorio raiz dos uploads, absoluto e normalizado. */
	public static Path diretorioBase() {
		String padrao = Paths.get(System.getProperty("user.home"), "relatorios-uploads").toString();
		return Paths.get(Config.getOuPadrao("upload.dir", padrao)).toAbsolutePath().normalize();
	}

	/** Subpasta de um tipo de upload, criada se ainda nao existir. */
	public static File pasta(String subpasta) {
		File dir = diretorioBase().resolve(subpasta).toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	/** URL publica de um arquivo salvo numa subpasta. */
	public static String url(String subpasta, String nomeArquivo) {
		return PREFIXO_URL + subpasta + "/" + nomeArquivo;
	}

	/**
	 * Converte uma URL /uploads/... de volta para o arquivo em disco. Devolve null
	 * se a URL nao for de upload ou se apontar para fora do diretorio base.
	 */
	public static File arquivoDaUrl(String url) {
		if (url == null || !url.startsWith(PREFIXO_URL)) {
			return null;
		}
		return resolverDentroDoBase(url.substring(PREFIXO_URL.length()));
	}

	/**
	 * Resolve um caminho relativo dentro do diretorio base. Devolve null se o
	 * resultado sair do diretorio base (ex.: "../../etc/passwd").
	 */
	public static File resolverDentroDoBase(String caminhoRelativo) {
		if (caminhoRelativo == null) {
			return null;
		}
		Path base = diretorioBase();
		Path arquivo = base.resolve(caminhoRelativo).normalize();
		return arquivo.startsWith(base) && !arquivo.equals(base) ? arquivo.toFile() : null;
	}

	/** Remove o arquivo de uma URL /uploads/..., se existir. Devolve true se apagou. */
	public static boolean excluirPorUrl(String url) {
		File arquivo = arquivoDaUrl(url);
		return arquivo != null && arquivo.isFile() && arquivo.delete();
	}

	/** Remove um arquivo de uma subpasta pelo nome, se existir. */
	public static boolean excluir(String subpasta, String nomeArquivo) {
		if (Texto.vazio(nomeArquivo)) {
			return false;
		}
		File arquivo = resolverDentroDoBase(subpasta + "/" + nomeArquivo);
		return arquivo != null && arquivo.isFile() && arquivo.delete();
	}

	// ---------------------------------------------------------------
	// Nomes de arquivo (funcoes puras)
	// ---------------------------------------------------------------

	/**
	 * Tira o caminho que alguns navegadores enviam junto com o nome
	 * ("C:\fotos\a.jpg" vira "a.jpg").
	 */
	public static String nomeSemCaminho(String nomeOriginal) {
		if (nomeOriginal == null) {
			return "";
		}
		String nome = nomeOriginal;
		int barra = Math.max(nome.lastIndexOf('\\'), nome.lastIndexOf('/'));
		return barra >= 0 ? nome.substring(barra + 1) : nome;
	}

	/** Extensao em minusculas, sem o ponto; "" se nao houver. */
	public static String extensao(String nomeArquivo) {
		String nome = nomeSemCaminho(nomeArquivo);
		int ponto = nome.lastIndexOf('.');
		return ponto > 0 && ponto < nome.length() - 1 ? nome.substring(ponto + 1).toLowerCase(Locale.ROOT) : "";
	}

	/**
	 * Gera o nome em disco: prefixo + marca de tempo + (nome original limpo, se
	 * pedido) + extensao. Caracteres fora de [A-Za-z0-9._-] viram "_", para que o
	 * nome seja seguro em URL e em qualquer sistema de arquivos.
	 */
	public static String gerarNome(String prefixo, String nomeOriginal, boolean manterNomeOriginal, long marcaTempo) {
		String nome = nomeSemCaminho(nomeOriginal);
		String ext = extensao(nome);
		StringBuilder sb = new StringBuilder();
		if (!Texto.vazio(prefixo)) {
			sb.append(prefixo).append('_');
		}
		sb.append(marcaTempo);
		if (manterNomeOriginal) {
			String base = ext.isEmpty() ? nome : nome.substring(0, nome.length() - ext.length() - 1);
			base = base.replaceAll("[^A-Za-z0-9._-]", "_");
			if (base.length() > 60) {
				base = base.substring(0, 60);
			}
			if (!base.isEmpty()) {
				sb.append('_').append(base);
			}
		}
		if (!ext.isEmpty()) {
			sb.append('.').append(ext);
		}
		return sb.toString();
	}

	/**
	 * Valida um arquivo enviado. Devolve a mensagem de erro para o usuario, ou
	 * null se o arquivo for aceito.
	 */
	public static String validar(String nomeArquivo, long tamanho, Set<String> extensoesAceitas) {
		if (Texto.vazio(nomeArquivo)) {
			return "Selecione um arquivo.";
		}
		String ext = extensao(nomeArquivo);
		if (!extensoesAceitas.contains(ext)) {
			return "Formato não aceito. Use: " + String.join(", ", ordenadas(extensoesAceitas)) + ".";
		}
		if (tamanho <= 0) {
			return "O arquivo enviado está vazio.";
		}
		if (tamanho > TAMANHO_MAXIMO) {
			return "O arquivo deve ter no máximo " + (TAMANHO_MAXIMO / (1024 * 1024)) + " MB.";
		}
		return null;
	}

	private static java.util.List<String> ordenadas(Set<String> extensoes) {
		java.util.List<String> lista = new java.util.ArrayList<>(extensoes);
		Collections.sort(lista);
		return lista;
	}

	// ---------------------------------------------------------------
	// Gravacao
	// ---------------------------------------------------------------

	/** Grava o conteudo numa subpasta com o nome dado e devolve o arquivo criado. */
	public static File salvar(InputStream conteudo, String subpasta, String nomeArquivo) throws IOException {
		File destino = resolverDentroDoBase(subpasta + "/" + nomeArquivo);
		if (destino == null) {
			throw new IOException("Nome de arquivo inválido: " + nomeArquivo);
		}
		pasta(subpasta);
		try (InputStream in = conteudo) {
			Files.copy(in, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return destino;
	}

	// ---------------------------------------------------------------
	// Tipo de conteudo (para o ImageServlet)
	// ---------------------------------------------------------------

	/** Tipo MIME pela extensao; application/octet-stream se desconhecida. */
	public static String tipoConteudo(String nomeArquivo) {
		switch (extensao(nomeArquivo)) {
		case "jpg":
		case "jpeg":
			return "image/jpeg";
		case "png":
			return "image/png";
		case "gif":
			return "image/gif";
		case "bmp":
			return "image/bmp";
		case "webp":
			return "image/webp";
		case "svg":
			return "image/svg+xml";
		case "ico":
			return "image/x-icon";
		case "pdf":
			return "application/pdf";
		default:
			return "application/octet-stream";
		}
	}

	/**
	 * Indica se o tipo pode ser exibido direto no navegador. SVG fica de fora de
	 * proposito: pode conter script, entao e servido como download (dentro de uma
	 * tag img o navegador continua desenhando normalmente).
	 */
	public static boolean exibivelNoNavegador(String tipoConteudo) {
		return TIPOS_EXIBIVEIS.contains(tipoConteudo);
	}
}

package com.augustopalhardi.relatorios.service.documento;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.augustopalhardi.relatorios.dto.ContatoEmpresa;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.SubtopicoRelatorio;
import com.augustopalhardi.relatorios.model.TopicoRelatorio;
import com.augustopalhardi.relatorios.util.HtmlSeguro;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;

/**
 * Monta o HTML do documento formatado (capa, identificacao, sumario, secoes 1 a
 * 6, fotos, assinatura e rodape) a partir do conteudo estruturado do relatorio.
 *
 * Funcao pura: recebe tudo por parametro e nao acessa banco, sessao nem JSF. As
 * classes CSS usadas (rta-*) estao em resources/css/VerificarRelatorio.css.
 */
public final class GeradorDocumento {

	private static final String SEPARADOR_PAGINA = "<div class='rta-separador-pagina'></div>";

	private GeradorDocumento() {
	}

	/**
	 * @param rel         relatorio de origem (dados e textos das secoes)
	 * @param topicos     topicos do relatorio, na ordem, com subtopicos
	 * @param empresa     dados da empresa prestadora
	 * @param contextPath context path da aplicacao, prefixado nas URLs de imagem
	 * @param anoAtual    ano usado na capa quando o relatorio nao tem data de inicio
	 */
	public static String gerar(Relatorio rel, List<TopicoRelatorio> topicos, ContatoEmpresa empresa,
			String contextPath, int anoAtual) {
		Contexto c = new Contexto(rel, empresa, contextPath, anoAtual);
		StringBuilder sb = new StringBuilder();
		capa(sb, c);
		sb.append(SEPARADOR_PAGINA);
		identificacao(sb, c);
		sb.append(SEPARADOR_PAGINA);
		sumario(sb, topicos);
		sb.append(SEPARADOR_PAGINA);
		secaoTexto(sb, 1, "APRESENTAÇÃO", rel.getApresentacao(), null);
		secaoTexto(sb, 2, "ESCOPO DO ATENDIMENTO", rel.getEscopo(), null);
		secaoTexto(sb, 3, "INTRODUÇÃO", rel.getIntroducao(), rel.getObservacao());
		atividades(sb, topicos, c);
		secaoTexto(sb, 5, "CONCLUSÃO DOS TRABALHOS", rel.getConclusao(), null);
		recomendacoes(sb, rel.getRecomendacoes());
		assinatura(sb, c);
		sb.append("<div class='rta-rodape'><p>").append(esc(empresa.linhaContato())).append("</p></div>");
		return sb.toString();
	}

	/** Ano exibido na capa: o da data de inicio, ou o ano atual. */
	public static int anoDaCapa(Date dataInicio, int anoAtual) {
		if (dataInicio == null) {
			return anoAtual;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataInicio);
		return cal.get(Calendar.YEAR);
	}

	/** Numero de secao no padrao do documento: 4.1, 4.1.2... */
	public static String numeroTopico(int indiceTopico) {
		return "4." + (indiceTopico + 1);
	}

	public static String numeroSubtopico(int indiceTopico, int indiceSubtopico) {
		return numeroTopico(indiceTopico) + "." + (indiceSubtopico + 1);
	}

	// ---------------------------------------------------------------
	// Partes do documento
	// ---------------------------------------------------------------

	private static void capa(StringBuilder sb, Contexto c) {
		sb.append("<div class='rta-capa'><div class='rta-capa-header'><div class='rta-capa-logo-box'>");
		if (c.empresa.temLogo()) {
			img(sb, c.contextPath + Uploads.url(Uploads.EMPRESA, c.empresa.arquivoLogo()), "rta-logo-empresa",
					"Logo Empresa");
		} else {
			img(sb, c.contextPath + "/resources/imagens/logo.png", "rta-logo-empresa", "Logo");
		}
		sb.append("</div>");
		if (!c.logoCliente.isEmpty()) {
			sb.append("<div class='rta-capa-logo-cliente-box'>");
			img(sb, c.contextPath + Uploads.url(Uploads.CLIENTES, c.logoCliente), "rta-logo-cliente", "Logo Cliente");
			sb.append("</div>");
		}
		sb.append("<div class='rta-capa-ano'>").append(c.ano).append("</div>");
		sb.append("</div><div class='rta-capa-body'>");
		sb.append("<div class='rta-capa-data-line'>").append(c.dataInicio).append("</div>");
		sb.append("<div class='rta-capa-titulo-principal'>").append(esc(c.titulo)).append("</div>");
		sb.append("</div></div>");
	}

	private static void identificacao(StringBuilder sb, Contexto c) {
		sb.append("<div class='rta-identificacao'>");
		sb.append("<h1 class='rta-title-main'>Relatório Técnico de Atividades</h1>");
		sb.append("<div class='rta-id-bloco'>");
		sb.append("<p><strong>Cliente:</strong> ").append(esc(c.nomeCliente)).append(", ").append(esc(c.localCliente))
				.append("</p>");
		sb.append("<p class='rta-destaque-cliente'><strong>Responsável Técnico Cliente:</strong> ")
				.append(esc(c.responsavelCliente)).append("</p>");
		sb.append("</div><div class='rta-id-bloco'>");
		sb.append("<p><strong>Datas do Atendimento:</strong> ").append(c.periodo).append("</p>");
		sb.append("<p class='rta-destaque-empresa'><strong>Responsável Técnico ").append(esc(c.empresa.nome()))
				.append(":</strong> ").append(esc(c.responsavelEmpresa)).append("</p>");
		sb.append("</div><div class='rta-contato-empresa'>");
		sb.append("<p>").append(esc(c.empresa.nome())).append("</p>");
		sb.append("<p>").append(esc(c.empresa.linhaContato())).append("</p>");
		sb.append("</div></div>");
	}

	private static void sumario(StringBuilder sb, List<TopicoRelatorio> topicos) {
		sb.append("<div class='rta-sumario'><h2 class='rta-section-header'>SUMÁRIO</h2>");
		sb.append("<table class='rta-sumario-tabela'>");
		linhaSumario(sb, "1", "APRESENTAÇÃO", "");
		linhaSumario(sb, "2", "ESCOPO DO ATENDIMENTO", "");
		linhaSumario(sb, "3", "INTRODUÇÃO", "");
		linhaSumario(sb, "4", "DESCRIÇÃO DAS ATIVIDADES REALIZADAS", "");
		if (topicos != null) {
			for (int i = 0; i < topicos.size(); i++) {
				TopicoRelatorio t = topicos.get(i);
				linhaSumario(sb, numeroTopico(i), esc(Texto.nvl(t.getTitulo())), "sub");
				List<SubtopicoRelatorio> subs = t.getSubtopicos();
				if (subs != null) {
					for (int j = 0; j < subs.size(); j++) {
						linhaSumario(sb, numeroSubtopico(i, j), esc(Texto.nvl(subs.get(j).getTitulo())), "subsub");
					}
				}
			}
		}
		linhaSumario(sb, "5", "CONCLUSÃO DOS TRABALHOS", "");
		linhaSumario(sb, "6", "RECOMENDAÇÕES GERAIS", "");
		sb.append("</table></div>");
	}

	/** nivel: "" (secao), "sub" (topico) ou "subsub" (subtopico); textoHtml ja escapado. */
	private static void linhaSumario(StringBuilder sb, String numero, String textoHtml, String nivel) {
		if (nivel.isEmpty()) {
			sb.append("<tr><td class='rta-sum-num'>").append(numero).append("</td><td>").append(textoHtml)
					.append("</td></tr>");
		} else {
			sb.append("<tr><td class='rta-sum-num rta-sum-").append(nivel).append("'>").append(numero)
					.append("</td><td class='rta-sum-").append(nivel).append("-text'>").append(textoHtml)
					.append("</td></tr>");
		}
	}

	private static void secaoTexto(StringBuilder sb, int numero, String titulo, String texto, String complemento) {
		abrirSecao(sb, numero, titulo);
		for (String paragrafo : Texto.paragrafos(texto)) {
			sb.append("<p class='rta-paragrafo'>").append(esc(paragrafo)).append("</p>");
		}
		if (!Texto.vazio(complemento)) {
			sb.append("<p class='rta-paragrafo'>").append(esc(complemento.trim())).append("</p>");
		}
		sb.append("</div>");
	}

	private static void atividades(StringBuilder sb, List<TopicoRelatorio> topicos, Contexto c) {
		abrirSecao(sb, 4, "DESCRIÇÃO DAS ATIVIDADES REALIZADAS");
		if (topicos != null) {
			for (int i = 0; i < topicos.size(); i++) {
				TopicoRelatorio topico = topicos.get(i);
				sb.append("<div class='rta-topico'>");
				sb.append("<h3 class='rta-topico-titulo'><span class='rta-num'>").append(numeroTopico(i))
						.append("</span> ").append(esc(Texto.nvl(topico.getTitulo()))).append("</h3>");
				conteudoRico(sb, topico.getConteudo());
				gridFotos(sb, topico.getListaImagens(), c);
				List<SubtopicoRelatorio> subs = topico.getSubtopicos();
				if (subs != null) {
					for (int j = 0; j < subs.size(); j++) {
						SubtopicoRelatorio sub = subs.get(j);
						sb.append("<div class='rta-subtopico'>");
						sb.append("<h4 class='rta-subtopico-titulo'><span class='rta-num'>")
								.append(numeroSubtopico(i, j)).append("</span> ").append(esc(Texto.nvl(sub.getTitulo())))
								.append("</h4>");
						conteudoRico(sb, sub.getConteudo());
						gridFotos(sb, sub.getListaImagens(), c);
						sb.append("</div>");
					}
				}
				sb.append("</div>");
			}
		}
		sb.append("</div>");
	}

	private static void conteudoRico(StringBuilder sb, String html) {
		if (Texto.temConteudoVisivel(html)) {
			sb.append("<div class='rta-conteudo-rico'>").append(HtmlSeguro.sanitizarConteudoRico(html)).append("</div>");
		}
	}

	/** Grid de fotos; a numeracao continua de um topico para o outro (Foto 1, Foto 2...). */
	private static void gridFotos(StringBuilder sb, List<String> urls, Contexto c) {
		if (urls == null || urls.isEmpty()) {
			return;
		}
		sb.append("<div class='rta-fotos-grid'>");
		for (String url : urls) {
			c.contadorFoto++;
			String u = Texto.nvl(url);
			String urlFinal = u.startsWith("/") ? c.contextPath + u : c.contextPath + Uploads.PREFIXO_URL + u;
			sb.append("<div class='rta-foto-item'>");
			sb.append("<img src='").append(esc(urlFinal)).append("' class='rta-foto' alt='Foto ").append(c.contadorFoto)
					.append("' style='display:block;' />");
			sb.append("<span class='rta-foto-legenda'>Foto ").append(c.contadorFoto).append("</span>");
			sb.append("</div>");
		}
		sb.append("</div>");
	}

	private static void recomendacoes(StringBuilder sb, String texto) {
		abrirSecao(sb, 6, "RECOMENDAÇÕES GERAIS");
		sb.append("<ul class='rta-recomendacoes'>");
		for (String item : Texto.paragrafos(texto)) {
			sb.append("<li>").append(esc(item)).append("</li>");
		}
		sb.append("</ul></div>");
	}

	private static void assinatura(StringBuilder sb, Contexto c) {
		sb.append("<div class='rta-assinatura'>");
		sb.append("<p class='rta-assinatura-texto'>As informações contidas neste relatório são precisas e refletem ")
				.append("fielmente os trabalhos realizados no período de <strong>").append(c.periodo)
				.append("</strong>, na empresa <strong>").append(esc(c.nomeCliente))
				.append("</strong>. Em caso de dúvidas, por favor entre em contato.</p>");
		sb.append("<div class='rta-assinatura-box'><div class='rta-assinatura-linha'></div>");
		sb.append("<p class='rta-assinatura-nome'>").append(esc(c.responsavelEmpresa)).append("</p>");
		sb.append("<p class='rta-assinatura-empresa'>").append(esc(c.empresa.nome())).append("</p>");
		sb.append("<p class='rta-assinatura-contato'>").append(esc(c.empresa.linhaAssinatura())).append("</p>");
		sb.append("<p class='rta-assinatura-site'>").append(esc(c.empresa.site())).append("</p>");
		sb.append("</div></div>");
	}

	private static void abrirSecao(StringBuilder sb, int numero, String titulo) {
		sb.append("<div class='rta-secao'><h2 class='rta-secao-titulo'><span class='rta-num'>").append(numero)
				.append("</span> ").append(titulo).append("</h2>");
	}

	private static void img(StringBuilder sb, String src, String classe, String alt) {
		sb.append("<img src='").append(esc(src)).append("' class='").append(classe).append("' alt='").append(alt)
				.append("' />");
	}

	private static String esc(String s) {
		return Texto.escaparHtml(s);
	}

	/** Valores do relatorio ja tratados, e o contador de fotos do documento. */
	private static final class Contexto {
		final ContatoEmpresa empresa;
		final String contextPath;
		final String nomeCliente;
		final String localCliente;
		final String logoCliente;
		final String responsavelCliente;
		final String responsavelEmpresa;
		final String titulo;
		final String dataInicio;
		final String periodo;
		final int ano;
		int contadorFoto = 0;

		Contexto(Relatorio rel, ContatoEmpresa empresa, String contextPath, int anoAtual) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
			this.empresa = empresa;
			this.contextPath = contextPath != null ? contextPath : "";
			this.nomeCliente = rel.getCliente() != null ? Texto.ouPadrao(rel.getCliente().getNomeEmpresa(), "-") : "-";
			this.localCliente = rel.getCliente() != null ? Texto.ouPadrao(rel.getCliente().getLocalEmpresa(), "-") : "-";
			this.logoCliente = rel.getCliente() != null ? Texto.nvl(rel.getCliente().getUrlLogo()) : "";
			this.responsavelCliente = Texto.nvl(rel.getResponsavelTecCliente());
			this.responsavelEmpresa = Texto.nvl(rel.getResponsavelTecnico());
			this.titulo = Texto.nvl(rel.getTitulo());
			this.dataInicio = rel.getDataInicio() != null ? fmt.format(rel.getDataInicio()) : "-";
			String dataTermino = rel.getDataTermino() != null ? fmt.format(rel.getDataTermino()) : "-";
			this.periodo = dataInicio + " a " + dataTermino;
			this.ano = anoDaCapa(rel.getDataInicio(), anoAtual);
		}
	}
}

package com.augustopalhardi.relatorios.util;

import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Sanitizacao do HTML produzido pelo editor rico (Quill) dos topicos e
 * subtopicos, antes de gravar e antes de inserir no documento formatado.
 *
 * Mantem a formatacao que o editor gera (negrito, listas, links, cores,
 * alinhamento) e remove scripts, handlers de evento (onclick, onerror...) e
 * URLs javascript:.
 */
public final class HtmlSeguro {

	/** Classes que o Quill usa para alinhamento, recuo e tamanho de fonte. */
	private static final String CLASSES_QUILL = "^(ql-[a-z0-9-]+\\s*)+$";

	private static final PolicyFactory POLITICA_EDITOR = new HtmlPolicyBuilder()
			.allowCommonInlineFormattingElements()
			.allowCommonBlockElements()
			.allowElements("pre", "a", "img")
			.allowStyling()
			.allowStandardUrlProtocols()
			.allowAttributes("href").onElements("a")
			.allowAttributes("target").matching(Pattern.compile("^_blank$")).onElements("a")
			.requireRelsOnLinks("noopener", "noreferrer")
			.allowAttributes("src", "alt", "width", "height").onElements("img")
			.allowAttributes("class").matching(Pattern.compile(CLASSES_QUILL))
			.onElements("p", "span", "li", "ol", "ul", "h1", "h2", "h3", "pre", "blockquote")
			.toFactory();

	private HtmlSeguro() {
	}

	/** Devolve o HTML do editor sem nada executavel; null vira "". */
	public static String sanitizarConteudoRico(String html) {
		if (html == null || html.isEmpty()) {
			return "";
		}
		return POLITICA_EDITOR.sanitize(html);
	}
}

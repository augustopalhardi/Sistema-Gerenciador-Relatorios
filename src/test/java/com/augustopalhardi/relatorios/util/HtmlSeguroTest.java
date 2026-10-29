package com.augustopalhardi.relatorios.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlSeguroTest {

	@Test
	void removeScriptsEHandlersDeEvento() {
		String limpo = HtmlSeguro.sanitizarConteudoRico(
				"<p onclick=\"alert(1)\">Texto</p><script>alert(2)</script><img src=x onerror=alert(3)>");
		assertFalse(limpo.contains("script"));
		assertFalse(limpo.contains("onclick"));
		assertFalse(limpo.contains("onerror"));
		assertTrue(limpo.contains("Texto"));
	}

	@Test
	void removeLinkJavascript() {
		String limpo = HtmlSeguro.sanitizarConteudoRico("<a href=\"javascript:alert(1)\">clique</a>");
		assertFalse(limpo.toLowerCase().contains("javascript"));
		assertTrue(limpo.contains("clique"));
	}

	@Test
	void mantemFormatacaoDoEditor() {
		String html = "<p class=\"ql-align-center\"><strong>Negrito</strong> <em>itálico</em> <u>sub</u></p>"
				+ "<ol><li>um</li></ol><p><span style=\"color:#d97706\">cor</span></p>";
		String limpo = HtmlSeguro.sanitizarConteudoRico(html);
		assertTrue(limpo.contains("<strong>Negrito</strong>"));
		assertTrue(limpo.contains("<em>"));
		assertTrue(limpo.contains("<u>"));
		assertTrue(limpo.contains("ql-align-center"));
		assertTrue(limpo.contains("<li>um</li>"));
		assertTrue(limpo.contains("color"));
	}

	@Test
	void classesForaDoPadraoDoEditorSaoDescartadas() {
		String limpo = HtmlSeguro.sanitizarConteudoRico("<p class=\"perigosa\">x</p>");
		assertFalse(limpo.contains("perigosa"));
	}

	@Test
	void nullViraVazio() {
		assertEquals("", HtmlSeguro.sanitizarConteudoRico(null));
	}
}

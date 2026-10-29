package com.augustopalhardi.relatorios.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class TextoTest {

	@Test
	void nvlTrataNullEApara() {
		assertEquals("", Texto.nvl(null));
		assertEquals("abc", Texto.nvl("  abc "));
	}

	@Test
	void ouPadraoUsaPadraoSoQuandoVazio() {
		assertEquals("-", Texto.ouPadrao("   ", "-"));
		assertEquals("-", Texto.ouPadrao(null, "-"));
		assertEquals("x", Texto.ouPadrao(" x ", "-"));
	}

	@Test
	void escaparHtmlNeutralizaTagsEAspas() {
		assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;", Texto.escaparHtml("<script>alert(\"x\")</script>"));
		assertEquals("O&#39;Brien &amp; Cia", Texto.escaparHtml("O'Brien & Cia"));
		assertEquals("", Texto.escaparHtml(null));
	}

	@Test
	void conteudoDoEditorVazioNaoContaComoTexto() {
		assertFalse(Texto.temConteudoVisivel(null));
		assertFalse(Texto.temConteudoVisivel("<p><br></p>"));
		assertFalse(Texto.temConteudoVisivel("<p>&nbsp;</p>"));
		assertTrue(Texto.temConteudoVisivel("<p>Troca do rolamento</p>"));
		assertTrue(Texto.temConteudoVisivel("<p><img src='a.png'></p>"));
	}

	@Test
	void paragrafosIgnoraLinhasEmBrancoEAceitaCrlf() {
		assertEquals(Arrays.asList("Primeira", "Segunda"), Texto.paragrafos("  Primeira \r\n\r\n   \nSegunda"));
		assertEquals(Collections.emptyList(), Texto.paragrafos(null));
	}

	@Test
	void rotuloNumeradoComESemTitulo() {
		assertEquals("Tópico 1 - Bombas", Texto.rotuloNumerado("Tópico", 0, " Bombas "));
		assertEquals("Tópico 3", Texto.rotuloNumerado("Tópico", 2, ""));
	}

	@Test
	void contemIgnorandoCaixa() {
		assertTrue(Texto.contemIgnorandoCaixa("Manutenção Preventiva", "PREVENT"));
		assertFalse(Texto.contemIgnorandoCaixa(null, "x"));
	}

	@Test
	void inicialDoNome() {
		assertEquals("M", Texto.inicial(" maria "));
		assertEquals("?", Texto.inicial("  "));
	}
}

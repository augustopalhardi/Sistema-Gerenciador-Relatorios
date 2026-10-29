package com.augustopalhardi.relatorios.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class ListaImagensTest {

	@Test
	void colunaVaziaViraListaVazia() {
		assertTrue(ListaImagens.lista(null).isEmpty());
		assertTrue(ListaImagens.lista("  ").isEmpty());
	}

	@Test
	void ignoraEntradasVazias() {
		assertEquals(Arrays.asList("/a.png", "/b.png"), ListaImagens.lista("/a.png;;/b.png;"));
	}

	@Test
	void adicionarERemover() {
		String coluna = ListaImagens.adicionar(null, "/a.png");
		coluna = ListaImagens.adicionar(coluna, "/b.png");
		coluna = ListaImagens.adicionar(coluna, "/c.png");
		assertEquals("/a.png;/b.png;/c.png", coluna);

		coluna = ListaImagens.remover(coluna, 1);
		assertEquals("/a.png;/c.png", coluna);
		assertEquals("/a.png;/c.png", ListaImagens.remover(coluna, 9));
		assertNull(ListaImagens.remover("/a.png", 0));
	}

	@Test
	void topicoUsaAsMesmasRegras() {
		TopicoRelatorio t = new TopicoRelatorio();
		t.adicionarImagem("/x.png");
		t.adicionarImagem("/y.png");
		t.removerImagem(0);
		assertEquals(Arrays.asList("/y.png"), t.getListaImagens());
	}
}

package com.augustopalhardi.relatorios.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * As imagens de topicos e subtopicos ficam gravadas numa unica coluna, como
 * URLs separadas por ";". Estas funcoes puras convertem entre a coluna e a
 * lista.
 */
public final class ListaImagens {

	static final String SEPARADOR = ";";

	private ListaImagens() {
	}

	/** Lista de URLs a partir do valor da coluna (null ou vazio = lista vazia). */
	public static List<String> lista(String coluna) {
		List<String> lista = new ArrayList<>();
		if (coluna == null || coluna.trim().isEmpty()) {
			return lista;
		}
		for (String url : Arrays.asList(coluna.split(SEPARADOR))) {
			if (!url.trim().isEmpty()) {
				lista.add(url.trim());
			}
		}
		return lista;
	}

	/** Novo valor da coluna com a URL acrescentada no fim. */
	public static String adicionar(String coluna, String url) {
		if (url == null || url.trim().isEmpty()) {
			return coluna;
		}
		List<String> lista = lista(coluna);
		lista.add(url.trim());
		return String.join(SEPARADOR, lista);
	}

	/** Novo valor da coluna sem a imagem da posicao indicada (indice invalido: sem mudanca). */
	public static String remover(String coluna, int indice) {
		List<String> lista = lista(coluna);
		if (indice < 0 || indice >= lista.size()) {
			return coluna;
		}
		lista.remove(indice);
		return lista.isEmpty() ? null : String.join(SEPARADOR, lista);
	}
}

package com.augustopalhardi.relatorios.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Funcoes puras de tratamento de texto usadas na montagem do documento e nas
 * telas. Nenhuma depende de JSF, JPA ou banco.
 */
public final class Texto {

	private Texto() {
	}

	/** Devolve o texto sem espacos nas pontas, ou "" se for null. */
	public static String nvl(String s) {
		return s != null ? s.trim() : "";
	}

	/** Devolve o texto aparado, ou o padrao se estiver vazio. */
	public static String ouPadrao(String s, String padrao) {
		String v = nvl(s);
		return v.isEmpty() ? padrao : v;
	}

	public static boolean vazio(String s) {
		return s == null || s.trim().isEmpty();
	}

	/** Aparar sem converter vazio em "", para campos opcionais que aceitam null. */
	public static String aparar(String s) {
		return s != null ? s.trim() : null;
	}

	/** Escapa texto para ser inserido como conteudo de um elemento HTML. */
	public static String escaparHtml(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	/** Escapa texto para ser usado dentro de um atributo HTML entre aspas. */
	public static String escaparAtributo(String s) {
		return escaparHtml(s);
	}

	/**
	 * Indica se um HTML vindo do editor rico tem algum texto visivel. O editor
	 * (Quill) grava "&lt;p&gt;&lt;br&gt;&lt;/p&gt;" quando o campo fica vazio.
	 * Imagens contam como conteudo.
	 */
	public static boolean temConteudoVisivel(String html) {
		if (html == null) {
			return false;
		}
		if (html.toLowerCase().contains("<img")) {
			return true;
		}
		String semTags = html.replaceAll("<[^>]*>", "").replace("&nbsp;", "").trim();
		return !semTags.isEmpty();
	}

	/** Quebra um texto em paragrafos: uma entrada por linha nao vazia, aparada. */
	public static List<String> paragrafos(String texto) {
		List<String> lista = new ArrayList<>();
		if (texto == null) {
			return lista;
		}
		for (String linha : texto.split("\\r?\\n")) {
			if (!linha.trim().isEmpty()) {
				lista.add(linha.trim());
			}
		}
		return lista;
	}

	/** Rotulo de opcao numerada: "Topico 2 - Titulo" ou so "Topico 2" sem titulo. */
	public static String rotuloNumerado(String prefixo, int indice, String titulo) {
		String rotulo = prefixo + " " + (indice + 1);
		return vazio(titulo) ? rotulo : rotulo + " - " + titulo.trim();
	}

	/** Contem, sem diferenciar maiusculas e minusculas; null nunca contem. */
	public static boolean contemIgnorandoCaixa(String texto, String termo) {
		return texto != null && termo != null && texto.toLowerCase().contains(termo.toLowerCase());
	}

	/** Primeira letra em maiuscula, ou "?" se o texto estiver vazio. */
	public static String inicial(String nome) {
		String n = nvl(nome);
		return n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
	}
}

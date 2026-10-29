package com.augustopalhardi.relatorios.service;

import java.util.ArrayList;
import java.util.List;

import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.RelatorioFormatado;
import com.augustopalhardi.relatorios.util.Texto;

/**
 * Busca em memoria usada nas listas de rascunhos. Funcoes puras: devolvem uma
 * nova lista e nao alteram a original.
 */
public final class FiltroRelatorios {

	private FiltroRelatorios() {
	}

	/** Relatorios cujo titulo, cliente ou responsavel tecnico contem o termo. */
	public static List<Relatorio> relatorios(List<Relatorio> todos, String termo) {
		if (Texto.vazio(termo)) {
			return new ArrayList<>(todos);
		}
		String t = termo.trim();
		List<Relatorio> resultado = new ArrayList<>();
		for (Relatorio r : todos) {
			String cliente = r.getCliente() != null ? r.getCliente().getNomeEmpresa() : null;
			if (Texto.contemIgnorandoCaixa(r.getTitulo(), t) || Texto.contemIgnorandoCaixa(cliente, t)
					|| Texto.contemIgnorandoCaixa(r.getResponsavelTecnico(), t)) {
				resultado.add(r);
			}
		}
		return resultado;
	}

	/** Rascunhos formatados cujo titulo ou cliente do relatorio de origem contem o termo. */
	public static List<RelatorioFormatado> formatados(List<RelatorioFormatado> todos, String termo) {
		if (Texto.vazio(termo)) {
			return new ArrayList<>(todos);
		}
		String t = termo.trim();
		List<RelatorioFormatado> resultado = new ArrayList<>();
		for (RelatorioFormatado rf : todos) {
			String cliente = rf.getRelatorio() != null && rf.getRelatorio().getCliente() != null
					? rf.getRelatorio().getCliente().getNomeEmpresa()
					: null;
			if (Texto.contemIgnorandoCaixa(rf.getTituloRascunho(), t) || Texto.contemIgnorandoCaixa(cliente, t)) {
				resultado.add(rf);
			}
		}
		return resultado;
	}
}

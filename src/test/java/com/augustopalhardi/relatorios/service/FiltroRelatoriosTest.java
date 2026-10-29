package com.augustopalhardi.relatorios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.RelatorioFormatado;

class FiltroRelatoriosTest {

	private static Relatorio rel(String titulo, String cliente, String responsavel) {
		Relatorio r = new Relatorio();
		r.setTitulo(titulo);
		if (cliente != null) {
			CadastroCliente c = new CadastroCliente();
			c.setNomeEmpresa(cliente);
			r.setCliente(c);
		}
		r.setResponsavelTecnico(responsavel);
		return r;
	}

	private final Relatorio preventiva = rel("Manutenção preventiva", "Metalúrgica Alfa", "João");
	private final Relatorio corretiva = rel("Corretiva bomba", null, "Maria");
	private final Relatorio semTitulo = rel(null, "Química Beta", null);
	private final List<Relatorio> todos = Arrays.asList(preventiva, corretiva, semTitulo);

	@Test
	void termoVazioDevolveCopiaComTodos() {
		List<Relatorio> r = FiltroRelatorios.relatorios(todos, "  ");
		assertEquals(todos, r);
		assertNotSame(todos, r);
	}

	@Test
	void buscaPorTituloClienteOuResponsavelSemDiferenciarCaixa() {
		assertEquals(Arrays.asList(preventiva), FiltroRelatorios.relatorios(todos, "PREVENT"));
		assertEquals(Arrays.asList(semTitulo), FiltroRelatorios.relatorios(todos, "beta"));
		assertEquals(Arrays.asList(corretiva), FiltroRelatorios.relatorios(todos, " maria "));
		assertEquals(0, FiltroRelatorios.relatorios(todos, "inexistente").size());
	}

	@Test
	void formatadosPorTituloOuClienteDoRelatorio() {
		RelatorioFormatado a = new RelatorioFormatado();
		a.setTituloRascunho("Documento final");
		a.setRelatorio(preventiva);
		RelatorioFormatado b = new RelatorioFormatado();
		b.setTituloRascunho("Outro");
		List<RelatorioFormatado> lista = Arrays.asList(a, b);

		assertEquals(Arrays.asList(a), FiltroRelatorios.formatados(lista, "alfa"));
		assertEquals(Arrays.asList(b), FiltroRelatorios.formatados(lista, "outro"));
		assertEquals(lista, FiltroRelatorios.formatados(lista, null));
	}
}

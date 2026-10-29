package com.augustopalhardi.relatorios.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

class RelatorioTest {

	private static TopicoRelatorio topico(String titulo, String... subtitulos) {
		TopicoRelatorio t = new TopicoRelatorio();
		t.setTitulo(titulo);
		for (String s : subtitulos) {
			SubtopicoRelatorio sub = new SubtopicoRelatorio();
			sub.setTitulo(s);
			t.getSubtopicos().add(sub);
		}
		return t;
	}

	private static Relatorio valido() {
		Relatorio r = new Relatorio();
		r.setCliente(new CadastroCliente());
		r.setTitulo("Manutenção preventiva");
		return r;
	}

	@Test
	void substituirTopicosNumeraELigaOsFilhos() {
		Relatorio r = new Relatorio();
		r.substituirTopicos(Arrays.asList(topico("A", "A1", "A2"), topico("B")));

		assertEquals(2, r.getTopicos().size());
		assertEquals(1, r.getTopicos().get(0).getNumero());
		assertEquals(2, r.getTopicos().get(1).getNumero());
		assertSame(r, r.getTopicos().get(1).getRelatorio());
		SubtopicoRelatorio a2 = r.getTopicos().get(0).getSubtopicos().get(1);
		assertEquals(2, a2.getNumero());
		assertSame(r.getTopicos().get(0), a2.getTopico());
	}

	@Test
	void substituirTopicosUsaAMesmaListaMapeada() {
		Relatorio r = new Relatorio();
		java.util.List<TopicoRelatorio> mapeada = r.getTopicos();
		r.substituirTopicos(Arrays.asList(topico("A")));
		r.substituirTopicos(Arrays.asList(topico("B"), topico("C")));
		// o JPA precisa da mesma instancia para detectar as remocoes (orphanRemoval)
		assertSame(mapeada, r.getTopicos());
		assertEquals("B", r.getTopicos().get(0).getTitulo());
	}

	@Test
	void duplicarCopiaConteudoSemIds() {
		Relatorio original = valido();
		original.setId(10L);
		original.setUsuarioRegistro("Ana");
		original.substituirTopicos(Arrays.asList(topico("A", "A1")));
		original.getTopicos().get(0).setId(5L);
		original.getTopicos().get(0).adicionarImagem("/uploads/relatorios/f.jpg");
		Date agora = new Date();

		Relatorio copia = original.duplicar("Bruno", agora);

		assertNull(copia.getId());
		assertEquals("Manutenção preventiva (Cópia)", copia.getTitulo());
		assertEquals("Bruno", copia.getUsuarioRegistro());
		assertEquals(agora, copia.getDataCriacao());
		assertSame(original.getCliente(), copia.getCliente());
		TopicoRelatorio t = copia.getTopicos().get(0);
		assertNull(t.getId());
		assertNotSame(original.getTopicos().get(0), t);
		assertSame(copia, t.getRelatorio());
		assertEquals("/uploads/relatorios/f.jpg", t.getImagens());
		assertEquals("A1", t.getSubtopicos().get(0).getTitulo());
		assertSame(t, t.getSubtopicos().get(0).getTopico());
	}

	@Test
	void duplicarSemUsuarioMantemORegistroOriginal() {
		Relatorio original = valido();
		original.setUsuarioRegistro("Ana");
		assertEquals("Ana", original.duplicar(null, new Date()).getUsuarioRegistro());
	}

	@Test
	void validacaoDosCamposObrigatoriosEDasDatas() {
		assertNull(valido().validar());

		Relatorio semCliente = valido();
		semCliente.setCliente(null);
		assertEquals("Selecione um cliente.", semCliente.validar());

		Relatorio semTitulo = valido();
		semTitulo.setTitulo("   ");
		assertEquals("Informe o título do relatório.", semTitulo.validar());

		Relatorio datasInvertidas = valido();
		datasInvertidas.setDataInicio(new Date(2_000_000_000L));
		datasInvertidas.setDataTermino(new Date(1_000_000_000L));
		assertEquals("A data de término não pode ser anterior à data de início.", datasInvertidas.validar());
	}
}

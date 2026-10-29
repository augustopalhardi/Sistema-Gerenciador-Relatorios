package com.augustopalhardi.relatorios.service.documento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.augustopalhardi.relatorios.dto.ContatoEmpresa;
import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.model.DadosEmpresa;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.SubtopicoRelatorio;
import com.augustopalhardi.relatorios.model.TopicoRelatorio;

class GeradorDocumentoTest {

	private static Relatorio relatorio() {
		CadastroCliente cliente = new CadastroCliente();
		cliente.setNomeEmpresa("Cliente Exemplo");
		cliente.setLocalEmpresa("Campinas/SP");
		cliente.setUrlLogo("cliente_1.png");
		Relatorio r = new Relatorio();
		r.setCliente(cliente);
		r.setTitulo("Inspeção <anual>");
		r.setResponsavelTecnico("Técnico Responsável");
		r.setResponsavelTecCliente("Gerente do Cliente");
		r.setDataInicio(new GregorianCalendar(2025, Calendar.MARCH, 10).getTime());
		r.setDataTermino(new GregorianCalendar(2025, Calendar.MARCH, 12).getTime());
		r.setApresentacao("Primeiro parágrafo\n\nSegundo parágrafo");
		r.setRecomendacoes("Trocar filtro\nRevisar painel");
		return r;
	}

	private static ContatoEmpresa empresa(String logo) {
		DadosEmpresa d = new DadosEmpresa();
		d.setNomeEmpresa("Empresa Exemplo");
		d.setTelefonePrincipal("(00) 0000-0000");
		d.setEmailContato("contato@example.com");
		d.setUrlLogo(logo);
		return ContatoEmpresa.de(d);
	}

	private static TopicoRelatorio topico(String titulo, String imagens, SubtopicoRelatorio... subs) {
		TopicoRelatorio t = new TopicoRelatorio();
		t.setTitulo(titulo);
		t.setImagens(imagens);
		for (SubtopicoRelatorio s : subs) {
			t.getSubtopicos().add(s);
		}
		return t;
	}

	private static SubtopicoRelatorio sub(String titulo, String conteudo, String imagens) {
		SubtopicoRelatorio s = new SubtopicoRelatorio();
		s.setTitulo(titulo);
		s.setConteudo(conteudo);
		s.setImagens(imagens);
		return s;
	}

	private static int ocorrencias(String texto, String trecho) {
		int n = 0;
		for (int i = texto.indexOf(trecho); i >= 0; i = texto.indexOf(trecho, i + 1)) {
			n++;
		}
		return n;
	}

	@Test
	void numeraTopicosESubtopicosNoSumarioENoCorpo() {
		List<TopicoRelatorio> topicos = Arrays.asList(
				topico("Bombas", null, sub("Bomba 1", null, null), sub("Bomba 2", null, null)),
				topico("Painéis", null));
		String html = GeradorDocumento.gerar(relatorio(), topicos, empresa(null), "/sistema-gerenciador-relatorios", 2026);

		assertTrue(html.contains(">4.1</td><td class='rta-sum-sub-text'>Bombas<"));
		assertTrue(html.contains(">4.1.2</td><td class='rta-sum-subsub-text'>Bomba 2<"));
		assertTrue(html.contains(">4.2</td><td class='rta-sum-sub-text'>Painéis<"));
		assertTrue(html.contains("<span class='rta-num'>4.1.1</span> Bomba 1</h4>"));
		assertTrue(html.contains("<span class='rta-num'>4.2</span> Painéis</h3>"));
	}

	@Test
	void numeracaoDasFotosContinuaEntreTopicos() {
		List<TopicoRelatorio> topicos = Arrays.asList(
				topico("A", "/uploads/relatorios/a.jpg;/uploads/relatorios/b.jpg",
						sub("A1", null, "/uploads/relatorios/c.jpg")),
				topico("B", "/uploads/relatorios/d.jpg"));
		String html = GeradorDocumento.gerar(relatorio(), topicos, empresa(null), "/sistema-gerenciador-relatorios", 2026);

		assertTrue(html.contains("Foto 4</span>"));
		assertFalse(html.contains("Foto 5</span>"));
		assertTrue(html.contains("src='/sistema-gerenciador-relatorios/uploads/relatorios/d.jpg'"));
	}

	@Test
	void escapaTextoDigitadoESanitizaConteudoRico() {
		List<TopicoRelatorio> topicos = Collections.singletonList(topico("T", null,
				sub("S", "<p>ok<script>alert(1)</script></p>", null)));
		String html = GeradorDocumento.gerar(relatorio(), topicos, empresa(null), "", 2026);

		assertTrue(html.contains("Inspeção &lt;anual&gt;"));
		assertFalse(html.contains("<anual>"));
		assertFalse(html.contains("<script>"));
		assertTrue(html.contains("<p>ok</p>"));
	}

	@Test
	void conteudoRicoVazioNaoGeraBloco() {
		List<TopicoRelatorio> topicos = Collections.singletonList(topico("T", null, sub("S", "<p><br></p>", null)));
		String html = GeradorDocumento.gerar(relatorio(), topicos, empresa(null), "", 2026);
		assertFalse(html.contains("rta-conteudo-rico"));
	}

	@Test
	void secoesDeTextoViramParagrafosERecomendacoesViramItens() {
		String html = GeradorDocumento.gerar(relatorio(), Collections.emptyList(), empresa(null), "", 2026);
		assertEquals(2, ocorrencias(html, "parágrafo</p>"));
		assertTrue(html.contains("<li>Trocar filtro</li><li>Revisar painel</li>"));
		// a secao 4 aparece mesmo sem topicos, como no sumario
		assertTrue(html.contains("<span class='rta-num'>4</span> DESCRIÇÃO DAS ATIVIDADES REALIZADAS</h2>"));
	}

	@Test
	void capaUsaLogosEAnoDaDataDeInicio() {
		String comLogo = GeradorDocumento.gerar(relatorio(), null, empresa("logo_empresa_1.png"), "/sistema-gerenciador-relatorios", 2026);
		assertTrue(comLogo.contains("src='/sistema-gerenciador-relatorios/uploads/empresa/logo_empresa_1.png'"));
		assertTrue(comLogo.contains("src='/sistema-gerenciador-relatorios/uploads/clientes/cliente_1.png'"));
		assertTrue(comLogo.contains("<div class='rta-capa-ano'>2025</div>"));

		String semLogo = GeradorDocumento.gerar(relatorio(), null, empresa(null), "/sistema-gerenciador-relatorios", 2026);
		assertTrue(semLogo.contains("src='/sistema-gerenciador-relatorios/resources/imagens/logo.png'"));
	}

	@Test
	void identificacaoEAssinatura() {
		String html = GeradorDocumento.gerar(relatorio(), null, empresa(null), "", 2026);
		assertTrue(html.contains("<strong>Cliente:</strong> Cliente Exemplo, Campinas/SP"));
		assertTrue(html.contains("<strong>Datas do Atendimento:</strong> 10/03/2025 a 12/03/2025"));
		assertTrue(html.contains("<strong>Responsável Técnico Empresa Exemplo:</strong> Técnico Responsável"));
		assertTrue(html.contains("<p class='rta-assinatura-nome'>Técnico Responsável</p>"));
		assertTrue(html.contains("<div class='rta-rodape'><p>(00) 0000-0000 | contato@example.com</p></div>"));
	}

	@Test
	void relatorioSemClienteNemDatas() {
		Relatorio r = new Relatorio();
		String html = GeradorDocumento.gerar(r, null, empresa(null), "", 2026);
		assertTrue(html.contains("<strong>Cliente:</strong> -, -"));
		assertTrue(html.contains("- a -"));
		assertTrue(html.contains("<div class='rta-capa-ano'>2026</div>"));
	}

	@Test
	void funcoesDeNumeracao() {
		assertEquals("4.3", GeradorDocumento.numeroTopico(2));
		assertEquals("4.1.5", GeradorDocumento.numeroSubtopico(0, 4));
		assertEquals(2026, GeradorDocumento.anoDaCapa(null, 2026));
	}
}

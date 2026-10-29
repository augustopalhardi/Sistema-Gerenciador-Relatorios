package com.augustopalhardi.relatorios.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.augustopalhardi.relatorios.model.DadosEmpresa;

class ContatoEmpresaTest {

	private static DadosEmpresa dados(String tel, String contato, String comercial, String site) {
		DadosEmpresa d = new DadosEmpresa();
		d.setNomeEmpresa(" Empresa Exemplo ");
		d.setTelefonePrincipal(tel);
		d.setEmailContato(contato);
		d.setEmailComercial(comercial);
		d.setSite(site);
		return d;
	}

	@Test
	void linhasDeContatoCompletas() {
		ContatoEmpresa c = ContatoEmpresa.de(dados("(00) 0000-0000", "contato@example.com", "comercial@example.com",
				"www.example.com"));
		assertEquals("Empresa Exemplo", c.nome());
		assertEquals("(00) 0000-0000 | contato@example.com | www.example.com", c.linhaContato());
		assertEquals("(00) 0000-0000 | comercial@example.com", c.linhaAssinatura());
	}

	@Test
	void semEmailComercialAssinaturaUsaOContato() {
		ContatoEmpresa c = ContatoEmpresa.de(dados("(00) 0000-0000", "contato@example.com", null, null));
		assertEquals("(00) 0000-0000 | contato@example.com", c.linhaAssinatura());
	}

	@Test
	void camposVaziosNaoDeixamSeparadorSobrando() {
		ContatoEmpresa c = ContatoEmpresa.de(dados(null, "contato@example.com", "", "  "));
		assertEquals("contato@example.com", c.linhaContato());
	}

	@Test
	void entidadeNulaViraTudoVazio() {
		ContatoEmpresa c = ContatoEmpresa.de(null);
		assertEquals("", c.linhaContato());
		assertFalse(c.temLogo());
	}

	@Test
	void temLogoQuandoHaArquivo() {
		DadosEmpresa d = dados(null, null, null, null);
		d.setUrlLogo("logo_empresa_1.png");
		assertTrue(ContatoEmpresa.de(d).temLogo());
	}
}

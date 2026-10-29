package com.augustopalhardi.relatorios.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RegrasAcessoTest {

	@Test
	void loginErrosERecursosSaoPublicos() {
		assertTrue(RegrasAcesso.ehPublico("/index.xhtml"));
		assertTrue(RegrasAcesso.ehPublico("/error/404.xhtml"));
		assertTrue(RegrasAcesso.ehPublico("/javax.faces.resource/css/Tema.css.xhtml"));
	}

	@Test
	void telasInternasNaoSaoPublicas() {
		assertFalse(RegrasAcesso.ehPublico("/home/home.xhtml"));
		assertFalse(RegrasAcesso.ehPublico("/usuario/CadastroUsuario.xhtml"));
		// a comparacao e exata: um sufixo parecido nao libera a tela
		assertFalse(RegrasAcesso.ehPublico("/x/index.xhtml"));
	}

	@Test
	void usuarioLogadoSoAbreTelasConhecidas() {
		assertTrue(RegrasAcesso.ehPermitido("/home/home.xhtml"));
		assertTrue(RegrasAcesso.ehPermitido("/relatorio/VerificarRelatorio.xhtml"));
		assertFalse(RegrasAcesso.ehPermitido("/WEB-INF/templates/layout.xhtml"));
		assertFalse(RegrasAcesso.ehPermitido("/conta/criarConta.xhtml"));
		assertFalse(RegrasAcesso.ehPermitido("/home/inexistente.xhtml"));
	}

	@Test
	void identificaATelaDeLogin() {
		assertTrue(RegrasAcesso.ehLogin("/index.xhtml"));
		assertFalse(RegrasAcesso.ehLogin("/error/500.xhtml"));
	}
}

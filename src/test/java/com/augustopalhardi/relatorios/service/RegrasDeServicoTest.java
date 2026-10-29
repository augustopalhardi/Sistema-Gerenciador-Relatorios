package com.augustopalhardi.relatorios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.augustopalhardi.relatorios.model.DadosEmpresa;

/** Regras puras dos servicos (as partes que nao dependem de banco). */
class RegrasDeServicoTest {

	@Test
	void validacaoDeNomeEEmailDoUsuario() {
		assertNull(ManterCadastroUsuarioService.validarNomeEmail("Ana", "ana@example.com"));
		assertEquals("Nome é obrigatório.", ManterCadastroUsuarioService.validarNomeEmail(" ", "ana@example.com"));
		assertEquals("E-mail é obrigatório.", ManterCadastroUsuarioService.validarNomeEmail("Ana", null));
		assertEquals("Informe um e-mail válido.", ManterCadastroUsuarioService.validarNomeEmail("Ana", "ana@"));
		assertEquals("Informe um e-mail válido.", ManterCadastroUsuarioService.validarNomeEmail("Ana", "ana example.com"));
	}

	@Test
	void motivoDoBloqueioDeExclusaoDeUsuario() {
		assertNull(ManterCadastroUsuarioService.motivoBloqueioExclusao("Ana", 0, 0));
		assertTrue(ManterCadastroUsuarioService.motivoBloqueioExclusao("Ana", 3, 0)
				.contains("vinculado a 3 relatório(s). "));
		assertTrue(ManterCadastroUsuarioService.motivoBloqueioExclusao("Ana", 0, 2)
				.contains("vinculado a 2 foto(s) de campo."));
		assertTrue(ManterCadastroUsuarioService.motivoBloqueioExclusao("Ana", 1, 4)
				.contains("1 relatório(s) e 4 foto(s) de campo"));
	}

	@Test
	void dadosPadraoDaEmpresaSaoFicticios() {
		DadosEmpresa d = ManterDadosEmpresaService.criarDadosPadrao();
		assertNull(d.getId());
		assertEquals("Empresa Exemplo Ltda", d.getNomeEmpresa());
		assertTrue(d.getEmailContato().endsWith("@example.com"));
	}

	@Test
	void regraNegocioSoLancaQuandoHaMensagem() {
		RegraNegocioException.seHouver(null);
		RegraNegocioException e = assertThrows(RegraNegocioException.class,
				() -> RegraNegocioException.seHouver("Mensagem para o usuário"));
		assertEquals("Mensagem para o usuário", e.getMessage());
	}
}

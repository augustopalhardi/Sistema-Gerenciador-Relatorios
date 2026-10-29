package com.augustopalhardi.relatorios.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhasTest {

	@Test
	void hashConfereComASenhaOriginal() {
		String hash = Senhas.gerarHash("segredo123");
		assertTrue(Senhas.ehHash(hash));
		assertTrue(Senhas.confere("segredo123", hash));
		assertFalse(Senhas.confere("segredo124", hash));
	}

	@Test
	void hashNaoGuardaSenhaEmTextoPuroEUsaSalt() {
		String h1 = Senhas.gerarHash("segredo123");
		String h2 = Senhas.gerarHash("segredo123");
		assertFalse(h1.contains("segredo123"));
		assertNotEquals(h1, h2);
	}

	@Test
	void espacosFazemParteDaSenha() {
		String hash = Senhas.gerarHash(" senha com espaco ");
		assertTrue(Senhas.confere(" senha com espaco ", hash));
		assertFalse(Senhas.confere("senha com espaco", hash));
	}

	@Test
	void valorGravadoQueNaoEhHashNuncaConfere() {
		// senha gravada em texto puro (dado antigo) nao pode autenticar
		assertFalse(Senhas.confere("123456", "123456"));
		assertFalse(Senhas.confere("123456", null));
		assertFalse(Senhas.confere(null, Senhas.gerarHash("x12345")));
	}

	@Test
	void gerarHashRecusaSenhaVazia() {
		assertThrows(IllegalArgumentException.class, () -> Senhas.gerarHash(""));
	}

	@Test
	void validacaoDeNovaSenha() {
		assertNull(Senhas.validarNova("abc123", "abc123"));
		assertNull(Senhas.validarNova("abc123", null));
		assertEquals("Informe a senha.", Senhas.validarNova("", null));
		assertEquals("A senha não pode ser formada só por espaços.", Senhas.validarNova("       ", null));
		assertEquals("A senha deve ter pelo menos 6 caracteres.", Senhas.validarNova("abc", null));
		assertEquals("A confirmação da senha não confere.", Senhas.validarNova("abc123", "abc124"));
	}
}

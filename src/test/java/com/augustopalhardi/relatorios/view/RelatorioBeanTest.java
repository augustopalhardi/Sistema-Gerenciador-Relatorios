package com.augustopalhardi.relatorios.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RelatorioBeanTest {

	@Test
	void indiceVindoDoSelectDeTopico() {
		assertEquals(Integer.valueOf(0), RelatorioBean.indice("0"));
		assertEquals(Integer.valueOf(3), RelatorioBean.indice("3"));
		assertNull(RelatorioBean.indice(null));
		assertNull(RelatorioBean.indice(""));
		assertNull(RelatorioBean.indice("-1"));
		assertNull(RelatorioBean.indice("-5"));
		assertNull(RelatorioBean.indice("abc"));
	}
}

package com.augustopalhardi.relatorios.util.jpa;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Libera o pool de conexoes quando a aplicacao e parada ou reimplantada. */
@WebListener
public class EncerramentoListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent evento) {
		EntityManagerProducer.fecharFactory();
	}
}

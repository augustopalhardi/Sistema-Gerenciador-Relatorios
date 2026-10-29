package com.augustopalhardi.relatorios.util.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.util.Config;

/**
 * Produz um EntityManager por requisicao (CDI) a partir de uma unica
 * EntityManagerFactory da aplicacao.
 *
 * URL, usuario e senha do banco vem do arquivo de configuracao externo (ver
 * {@link Config}), nao do persistence.xml, para que credenciais nao sejam
 * versionadas nem empacotadas dentro do .war.
 */
@ApplicationScoped
public class EntityManagerProducer {

	public static final String UNIDADE_PERSISTENCIA = "relatoriosPU";

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerProducer.class);
	private static volatile EntityManagerFactory factory;

	private static EntityManagerFactory factory() {
		if (factory == null) {
			synchronized (EntityManagerProducer.class) {
				if (factory == null) {
					LOGGER.info("Criando EntityManagerFactory ({})", UNIDADE_PERSISTENCIA);
					factory = Persistence.createEntityManagerFactory(UNIDADE_PERSISTENCIA, propriedadesConexao());
				}
			}
		}
		return factory;
	}

	private static Map<String, String> propriedadesConexao() {
		Map<String, String> props = new HashMap<>();
		props.put("javax.persistence.jdbc.url", Config.get("db.url"));
		props.put("javax.persistence.jdbc.user", Config.get("db.user"));
		props.put("javax.persistence.jdbc.password", Config.get("db.password"));
		return props;
	}

	@Produces
	@RequestScoped
	public EntityManager createEntityManager() {
		return factory().createEntityManager();
	}

	public void closeEntityManager(@Disposes EntityManager manager) {
		if (manager.isOpen()) {
			manager.close();
		}
	}

	/** Fecha a factory (chamado quando a aplicacao e parada). */
	public static void fecharFactory() {
		if (factory != null && factory.isOpen()) {
			LOGGER.info("Fechando EntityManagerFactory");
			factory.close();
		}
	}
}

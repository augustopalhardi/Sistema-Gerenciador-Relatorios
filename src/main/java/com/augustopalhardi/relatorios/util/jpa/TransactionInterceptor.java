package com.augustopalhardi.relatorios.util.jpa;

import java.io.Serializable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abre a transacao no primeiro metodo {@link Transactional} da chamada e faz
 * commit (ou rollback, em caso de excecao) ao sair dele. Chamadas aninhadas
 * participam da mesma transacao.
 */
@Interceptor
@Transactional
public class TransactionInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInterceptor.class);

	@Inject
	private EntityManager manager;

	@AroundInvoke
	public Object invoke(InvocationContext context) throws Exception {
		EntityTransaction transaction = manager.getTransaction();
		boolean dono = false;
		try {
			if (!transaction.isActive()) {
				transaction.begin();
				dono = true;
			}
			Object resultado = context.proceed();
			if (dono && transaction.isActive()) {
				transaction.commit();
			}
			return resultado;
		} catch (Exception e) {
			if (dono && transaction.isActive()) {
				try {
					transaction.rollback();
					LOGGER.debug("Rollback em {}: {}", context.getMethod().getName(), e.toString());
				} catch (RuntimeException erroRollback) {
					LOGGER.error("Erro ao fazer rollback", erroRollback);
				}
			}
			throw e;
		}
	}
}

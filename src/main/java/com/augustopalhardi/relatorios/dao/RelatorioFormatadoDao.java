package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.augustopalhardi.relatorios.model.RelatorioFormatado;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class RelatorioFormatadoDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Transactional
	public RelatorioFormatado salvar(RelatorioFormatado rf) {
		return manager.merge(rf);
	}

	public RelatorioFormatado buscarPorId(Long id) {
		return manager.find(RelatorioFormatado.class, id);
	}

	public List<RelatorioFormatado> buscarTodos() {
		return manager.createQuery("SELECT rf FROM RelatorioFormatado rf ORDER BY rf.dataAtualizacao DESC",
				RelatorioFormatado.class).getResultList();
	}

	/** Quantidade de documentos formatados gerados a partir de um relatorio. */
	public long contarPorRelatorioId(Long relatorioId) {
		return manager
				.createQuery("SELECT COUNT(rf) FROM RelatorioFormatado rf WHERE rf.relatorio.id = :id", Long.class)
				.setParameter("id", relatorioId).getSingleResult();
	}

	@Transactional
	public void excluir(RelatorioFormatado rf) {
		manager.remove(manager.merge(rf));
	}
}

package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.augustopalhardi.relatorios.model.FotoCampo;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class FotoCampoDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Transactional
	public FotoCampo salvar(FotoCampo foto) {
		return manager.merge(foto);
	}

	public FotoCampo buscarPorId(Long id) {
		return manager.find(FotoCampo.class, id);
	}

	/** Todas as fotos de um relatorio (pendentes e utilizadas). */
	public List<FotoCampo> buscarPorRelatorioId(Long relatorioId) {
		return manager.createQuery(
				"SELECT f FROM FotoCampo f WHERE f.relatorio.id = :id ORDER BY f.dataUpload DESC", FotoCampo.class)
				.setParameter("id", relatorioId).getResultList();
	}

	/** Fotos ainda nao alocadas a nenhum topico. */
	public List<FotoCampo> buscarPendentesPorRelatorioId(Long relatorioId) {
		return manager.createQuery(
				"SELECT f FROM FotoCampo f WHERE f.relatorio.id = :id AND f.utilizada = false ORDER BY f.dataUpload DESC",
				FotoCampo.class).setParameter("id", relatorioId).getResultList();
	}

	/** Quantidade de fotos enviadas por um usuario. */
	public long contarPorUsuario(Long usuarioId) {
		return manager.createQuery("SELECT COUNT(f) FROM FotoCampo f WHERE f.usuario.id = :id", Long.class)
				.setParameter("id", usuarioId).getSingleResult();
	}

	@Transactional
	public void excluir(FotoCampo foto) {
		manager.remove(manager.merge(foto));
	}
}

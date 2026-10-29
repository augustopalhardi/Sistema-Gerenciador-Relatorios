package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.TopicoRelatorio;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class RelatorioDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Transactional
	public Relatorio salvar(Relatorio relatorio) {
		return manager.merge(relatorio);
	}

	public Relatorio buscarPeloCodigo(Long id) {
		return manager.find(Relatorio.class, id);
	}

	public List<Relatorio> buscarTodos() {
		return manager.createQuery("SELECT r FROM Relatorio r ORDER BY r.dataCriacao DESC", Relatorio.class)
				.getResultList();
	}

	/** Quantidade de relatorios de um cliente. */
	public long contarPorCliente(Long clienteId) {
		return manager.createQuery("SELECT COUNT(r) FROM Relatorio r WHERE r.cliente.id = :id", Long.class)
				.setParameter("id", clienteId).getSingleResult();
	}

	/**
	 * Quantidade de relatorios registrados por um usuario. O vinculo e pelo nome
	 * gravado em usuarioRegistro no momento da criacao.
	 */
	public long contarPorUsuarioRegistro(String nomeUsuario) {
		return manager.createQuery("SELECT COUNT(r) FROM Relatorio r WHERE r.usuarioRegistro = :nome", Long.class)
				.setParameter("nome", nomeUsuario).getSingleResult();
	}

	/**
	 * Busca os topicos (com subtopicos) direto por JPQL, forcando releitura do
	 * banco. Navegar pela relacao da entidade devolvia instancias do cache L1 com
	 * estado desatualizado durante a formatacao.
	 */
	public List<TopicoRelatorio> buscarTopicosPorRelatorioId(Long relatorioId) {
		TypedQuery<TopicoRelatorio> q = manager.createQuery("SELECT t FROM TopicoRelatorio t "
				+ "LEFT JOIN FETCH t.subtopicos s WHERE t.relatorio.id = :relatorioId ORDER BY t.numero ASC",
				TopicoRelatorio.class);
		q.setParameter("relatorioId", relatorioId);
		q.setHint("javax.persistence.cache.storeMode", "REFRESH");
		// o JOIN FETCH repete o topico uma vez por subtopico
		return q.getResultList().stream().distinct().collect(Collectors.toList());
	}

	@Transactional
	public void excluir(Relatorio relatorio) {
		manager.remove(manager.merge(relatorio));
	}
}

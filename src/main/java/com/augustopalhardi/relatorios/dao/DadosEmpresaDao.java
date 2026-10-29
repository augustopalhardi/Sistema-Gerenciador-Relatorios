package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.augustopalhardi.relatorios.model.DadosEmpresa;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class DadosEmpresaDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	/**
	 * Busca o único registro de dados da empresa. Retorna null se nenhum registro
	 * existir.
	 */
	public DadosEmpresa buscarDadosEmpresa() {
		String jpql = "SELECT d FROM DadosEmpresa d ORDER BY d.id ASC";
		TypedQuery<DadosEmpresa> q = manager.createQuery(jpql, DadosEmpresa.class);
		q.setMaxResults(1);
		List<DadosEmpresa> lista = q.getResultList();
		return lista.isEmpty() ? null : lista.get(0);
	}

	@Transactional
	public DadosEmpresa salvar(DadosEmpresa dados) {
		return manager.merge(dados);
	}
}

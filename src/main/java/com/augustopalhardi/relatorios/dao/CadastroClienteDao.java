package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class CadastroClienteDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Transactional
	public CadastroCliente salvar(CadastroCliente cadastroCliente) {
		return manager.merge(cadastroCliente);
	}

	public CadastroCliente buscarPeloCodigo(Long id) {
		return manager.find(CadastroCliente.class, id);
	}

	public List<CadastroCliente> buscarTodos() {
		return manager.createQuery("SELECT c FROM CadastroCliente c ORDER BY c.nomeEmpresa", CadastroCliente.class)
				.getResultList();
	}

	@Transactional
	public void excluir(CadastroCliente cadastroCliente) {
		manager.remove(manager.merge(cadastroCliente));
	}
}

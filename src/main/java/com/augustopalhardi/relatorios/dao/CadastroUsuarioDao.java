package com.augustopalhardi.relatorios.dao;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@RequestScoped
public class CadastroUsuarioDao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Transactional
	public CadastroUsuario salvar(CadastroUsuario cadastroUsuario) {
		return manager.merge(cadastroUsuario);
	}

	public CadastroUsuario buscarPeloCodigo(Long id) {
		return manager.find(CadastroUsuario.class, id);
	}

	public List<CadastroUsuario> buscarTodos() {
		return manager.createQuery("SELECT u FROM CadastroUsuario u ORDER BY u.nome", CadastroUsuario.class)
				.getResultList();
	}

	/** Usuario pelo e-mail (sem diferenciar maiusculas), ou null se nao houver. */
	public CadastroUsuario buscarPorEmail(String email) {
		List<CadastroUsuario> lista = manager
				.createQuery("SELECT u FROM CadastroUsuario u WHERE LOWER(u.email) = LOWER(:email)",
						CadastroUsuario.class)
				.setParameter("email", email).setMaxResults(1).getResultList();
		return lista.isEmpty() ? null : lista.get(0);
	}

	public long contarUsuarios() {
		return manager.createQuery("SELECT COUNT(u) FROM CadastroUsuario u", Long.class).getSingleResult();
	}

	@Transactional
	public void excluir(CadastroUsuario cadastroUsuario) {
		manager.remove(manager.merge(cadastroUsuario));
	}
}

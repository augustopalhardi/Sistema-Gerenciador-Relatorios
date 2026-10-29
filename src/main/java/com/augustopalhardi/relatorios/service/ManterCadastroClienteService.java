package com.augustopalhardi.relatorios.service;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.CadastroClienteDao;
import com.augustopalhardi.relatorios.dao.RelatorioDao;
import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@ApplicationScoped
public class ManterCadastroClienteService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroClienteDao cadastroClienteDao;

	@Inject
	private RelatorioDao relatorioDao;

	public CadastroCliente buscarPorId(Long clienteId) {
		return cadastroClienteDao.buscarPeloCodigo(clienteId);
	}

	public List<CadastroCliente> buscarTodos() {
		return cadastroClienteDao.buscarTodos();
	}

	@Transactional
	public CadastroCliente salvar(CadastroCliente cliente) {
		if (Texto.vazio(cliente.getNomeEmpresa())) {
			throw new RegraNegocioException("Nome da empresa é obrigatório.");
		}
		return cadastroClienteDao.salvar(cliente);
	}

	/**
	 * Exclui o cliente e o arquivo do seu logo. Bloqueia a exclusao se houver
	 * relatorios do cliente.
	 */
	@Transactional
	public void excluir(CadastroCliente cliente) {
		long relatorios = relatorioDao.contarPorCliente(cliente.getId());
		if (relatorios > 0) {
			throw new RegraNegocioException("Não é possível excluir o cliente \"" + cliente.getNomeEmpresa()
					+ "\": ele possui " + relatorios + " relatório(s). Exclua esses relatórios antes.");
		}
		cadastroClienteDao.excluir(cliente);
		Uploads.excluir(Uploads.CLIENTES, cliente.getUrlLogo());
	}
}

package com.augustopalhardi.relatorios.service;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.DadosEmpresaDao;
import com.augustopalhardi.relatorios.model.DadosEmpresa;
import com.augustopalhardi.relatorios.util.Uploads;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@ApplicationScoped
public class ManterDadosEmpresaService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DadosEmpresaDao dao;

	/**
	 * Busca os dados da empresa. Se ainda nao houver registro, devolve um objeto
	 * (nao gravado) com valores de exemplo.
	 */
	public DadosEmpresa buscarDadosEmpresa() {
		DadosEmpresa dados = dao.buscarDadosEmpresa();
		return dados != null ? dados : criarDadosPadrao();
	}

	@Transactional
	public DadosEmpresa salvar(DadosEmpresa dados) {
		return dao.salvar(dados);
	}

	/** Remove o logo (registro e arquivo em disco). */
	@Transactional
	public DadosEmpresa removerLogo(DadosEmpresa dados) {
		String arquivo = dados.getUrlLogo();
		dados.setUrlLogo(null);
		DadosEmpresa salvo = dao.salvar(dados);
		Uploads.excluir(Uploads.EMPRESA, arquivo);
		return salvo;
	}

	/**
	 * Dados de exemplo exibidos enquanto a empresa ainda nao foi configurada na
	 * tela "Minha Empresa". Usa o dominio reservado example.com.
	 */
	static DadosEmpresa criarDadosPadrao() {
		DadosEmpresa dados = new DadosEmpresa();
		dados.setNomeEmpresa("Empresa Exemplo Ltda");
		dados.setTelefonePrincipal("(00) 0000-0000");
		dados.setEmailContato("contato@example.com");
		dados.setEmailComercial("comercial@example.com");
		dados.setSite("www.example.com");
		return dados;
	}
}

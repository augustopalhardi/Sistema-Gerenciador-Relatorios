package com.augustopalhardi.relatorios.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.RelatorioFormatadoDao;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.RelatorioFormatado;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@ApplicationScoped
public class ManterRelatorioFormatadoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RelatorioFormatadoDao dao;

	public RelatorioFormatado buscarPorId(Long id) {
		return dao.buscarPorId(id);
	}

	public List<RelatorioFormatado> buscarTodos() {
		return dao.buscarTodos();
	}

	/**
	 * Grava o documento editado. Com rascunhoId, atualiza o rascunho existente;
	 * sem ele (ou se o rascunho nao existir mais), cria um novo.
	 */
	@Transactional
	public RelatorioFormatado salvarRascunho(Long rascunhoId, Relatorio relatorio, String conteudoHtml,
			String usuarioRegistro) {
		if (Texto.vazio(conteudoHtml)) {
			throw new RegraNegocioException("Não há conteúdo para salvar. Formate um relatório primeiro.");
		}
		if (relatorio == null) {
			throw new RegraNegocioException("Selecione e formate um relatório antes de salvar.");
		}
		Date agora = new Date();
		RelatorioFormatado rf = rascunhoId != null ? dao.buscarPorId(rascunhoId) : null;
		if (rf == null) {
			rf = new RelatorioFormatado();
			rf.setDataCriacao(agora);
		}
		rf.setDataAtualizacao(agora);
		rf.setRelatorio(relatorio);
		rf.setConteudoHtml(conteudoHtml);
		rf.setUsuarioRegistro(usuarioRegistro);
		rf.setTituloRascunho(Texto.ouPadrao(relatorio.getTitulo(), "Relatório"));
		return dao.salvar(rf);
	}

	@Transactional
	public void excluir(RelatorioFormatado rf) {
		dao.excluir(rf);
	}
}

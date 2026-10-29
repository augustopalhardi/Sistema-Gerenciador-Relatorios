package com.augustopalhardi.relatorios.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.FotoCampoDao;
import com.augustopalhardi.relatorios.dao.RelatorioDao;
import com.augustopalhardi.relatorios.dao.RelatorioFormatadoDao;
import com.augustopalhardi.relatorios.model.FotoCampo;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.TopicoRelatorio;
import com.augustopalhardi.relatorios.util.HtmlSeguro;
import com.augustopalhardi.relatorios.util.Uploads;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

@ApplicationScoped
public class ManterRelatorioService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RelatorioDao relatorioDao;

	@Inject
	private FotoCampoDao fotoCampoDao;

	@Inject
	private RelatorioFormatadoDao relatorioFormatadoDao;

	public Relatorio buscarPorId(Long id) {
		return relatorioDao.buscarPeloCodigo(id);
	}

	public List<Relatorio> buscarTodos() {
		return relatorioDao.buscarTodos();
	}

	public List<TopicoRelatorio> buscarTopicosPorRelatorioId(Long relatorioId) {
		return relatorioDao.buscarTopicosPorRelatorioId(relatorioId);
	}

	/**
	 * Valida e grava o relatorio com os topicos na ordem informada. Num relatorio
	 * novo (sem ID), registra a data de criacao e quem criou. As fotos de campo
	 * alocadas nos topicos durante a edicao so saem da lista de pendentes aqui,
	 * junto com o relatorio: se a edicao for abandonada, elas continuam pendentes.
	 */
	@Transactional
	public Relatorio salvar(Relatorio relatorio, List<TopicoRelatorio> topicos, String usuarioRegistro,
			Collection<Long> fotosCampoAlocadas) {
		RegraNegocioException.seHouver(relatorio.validar());
		for (TopicoRelatorio t : topicos) {
			t.setConteudo(HtmlSeguro.sanitizarConteudoRico(t.getConteudo()));
			t.getSubtopicos().forEach(s -> s.setConteudo(HtmlSeguro.sanitizarConteudoRico(s.getConteudo())));
		}
		relatorio.substituirTopicos(topicos);
		if (relatorio.getId() == null) {
			relatorio.setDataCriacao(new Date());
			relatorio.setUsuarioRegistro(usuarioRegistro);
		}
		Relatorio salvo = relatorioDao.salvar(relatorio);
		for (Long fotoId : fotosCampoAlocadas) {
			FotoCampo foto = fotoCampoDao.buscarPorId(fotoId);
			if (foto != null) {
				foto.setUtilizada(true);
				fotoCampoDao.salvar(foto);
			}
		}
		return salvo;
	}

	/** Cria e grava uma copia do relatorio (ver {@link Relatorio#duplicar}). */
	@Transactional
	public Relatorio duplicar(Relatorio original, String usuarioRegistro) {
		Relatorio completo = relatorioDao.buscarPeloCodigo(original.getId());
		if (completo == null) {
			throw new RegraNegocioException("Relatório não encontrado. Ele pode ter sido excluído.");
		}
		return relatorioDao.salvar(completo.duplicar(usuarioRegistro, new Date()));
	}

	/**
	 * Exclui o relatorio e suas fotos de campo numa unica transacao. Bloqueia a
	 * exclusao se ja houver documento formatado gerado a partir dele.
	 */
	@Transactional
	public void excluir(Relatorio relatorio) {
		if (relatorioFormatadoDao.contarPorRelatorioId(relatorio.getId()) > 0) {
			throw new RegraNegocioException("Este relatório já foi formatado e não pode ser excluído. "
					+ "Exclua antes a formatação em Rascunhos Formatados.");
		}
		List<FotoCampo> fotos = fotoCampoDao.buscarPorRelatorioId(relatorio.getId());
		for (FotoCampo foto : fotos) {
			fotoCampoDao.excluir(foto);
		}
		relatorioDao.excluir(relatorio);
		fotos.forEach(f -> Uploads.excluirPorUrl(f.getUrlFoto()));
	}
}

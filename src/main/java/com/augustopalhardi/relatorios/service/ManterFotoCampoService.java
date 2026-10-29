package com.augustopalhardi.relatorios.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.augustopalhardi.relatorios.dao.FotoCampoDao;
import com.augustopalhardi.relatorios.model.CadastroUsuario;
import com.augustopalhardi.relatorios.model.FotoCampo;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;
import com.augustopalhardi.relatorios.util.jpa.Transactional;

/**
 * Fotos enviadas em campo: ficam pendentes ate o tecnico aloca-las a um topico
 * ou subtopico do relatorio.
 */
@ApplicationScoped
public class ManterFotoCampoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FotoCampoDao fotoCampoDao;

	public FotoCampo buscarPorId(Long id) {
		return fotoCampoDao.buscarPorId(id);
	}

	public List<FotoCampo> buscarPendentesPorRelatorioId(Long relatorioId) {
		return fotoCampoDao.buscarPendentesPorRelatorioId(relatorioId);
	}

	/** Registra uma foto ja gravada em disco como pendente do relatorio. */
	@Transactional
	public FotoCampo registrar(Relatorio relatorio, CadastroUsuario usuario, String urlFoto, String legenda) {
		FotoCampo foto = new FotoCampo();
		foto.setRelatorio(relatorio);
		foto.setUsuario(usuario);
		foto.setUrlFoto(urlFoto);
		foto.setLegenda(Texto.nvl(legenda));
		foto.setDataUpload(new Date());
		foto.setUtilizada(false);
		return fotoCampoDao.salvar(foto);
	}

	/** Exclui a foto e o arquivo em disco. */
	@Transactional
	public void descartar(FotoCampo foto) {
		fotoCampoDao.excluir(foto);
		Uploads.excluirPorUrl(foto.getUrlFoto());
	}
}

package com.augustopalhardi.relatorios.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.augustopalhardi.relatorios.model.CadastroCliente;
import com.augustopalhardi.relatorios.model.FotoCampo;
import com.augustopalhardi.relatorios.model.Relatorio;
import com.augustopalhardi.relatorios.model.SubtopicoRelatorio;
import com.augustopalhardi.relatorios.model.TopicoRelatorio;
import com.augustopalhardi.relatorios.service.ManterCadastroClienteService;
import com.augustopalhardi.relatorios.service.ManterFotoCampoService;
import com.augustopalhardi.relatorios.service.ManterRelatorioService;
import com.augustopalhardi.relatorios.service.RegraNegocioException;
import com.augustopalhardi.relatorios.util.Texto;
import com.augustopalhardi.relatorios.util.Uploads;

/**
 * Tela de redacao do relatorio: dados do atendimento, secoes de texto, topicos
 * e subtopicos com imagens, e o painel de fotos de campo pendentes.
 */
@Named
@ViewScoped
public class RelatorioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RelatorioBean.class);

	/** Valor do select quando nenhum topico/subtopico foi escolhido. */
	private static final String NENHUM = "-1";

	@Inject
	private ManterRelatorioService relatorioService;

	@Inject
	private ManterCadastroClienteService clienteService;

	@Inject
	private ManterFotoCampoService fotoCampoService;

	private Relatorio relatorio;
	private List<CadastroCliente> clientesDisponiveis = new ArrayList<>();
	private Long clienteSelecionadoId;
	private List<TopicoRelatorio> topicosEdicao = new ArrayList<>();
	private boolean modoEdicao;

	private UploadedFile imagemTopicoUpload;
	private int topicoUploadIndex = -1;
	private UploadedFile imagemSubtopicoUpload;
	private int subtopicoUploadTopicoIndex = -1;
	private int subtopicoUploadIndex = -1;

	private List<FotoCampo> fotosCampoPendentes = new ArrayList<>();
	/** Destino escolhido para cada foto pendente: ID da foto -> indice (como texto, vindo do select). */
	private Map<Long, String> fotoTopicoIndexMap = new HashMap<>();
	private Map<Long, String> fotoSubtopicoIndexMap = new HashMap<>();
	/** Fotos colocadas nos topicos nesta edicao; viram "utilizadas" ao salvar o relatorio. */
	private Set<Long> fotosAlocadas = new HashSet<>();

	@PostConstruct
	public void inicializar() {
		limpar();
		try {
			clientesDisponiveis = clienteService.buscarTodos();
			Long relatorioId = (Long) SessaoUsuario.retirar(RascunhosBean.CHAVE_RELATORIO_A_EDITAR);
			if (relatorioId != null) {
				carregarRelatorioParaEdicao(relatorioId);
			}
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao abrir a tela de relatório", e);
			Mensagens.erroInesperado();
		}
	}

	private void carregarRelatorioParaEdicao(Long relatorioId) {
		Relatorio rel = relatorioService.buscarPorId(relatorioId);
		if (rel == null) {
			Mensagens.aviso("O relatório escolhido não foi encontrado. Ele pode ter sido excluído; "
					+ "o formulário abaixo é de um relatório novo.");
			return;
		}
		relatorio = rel;
		clienteSelecionadoId = rel.getCliente() != null ? rel.getCliente().getId() : null;
		topicosEdicao = relatorioService.buscarTopicosPorRelatorioId(relatorioId);
		modoEdicao = true;
		carregarFotosCampoPendentes();
	}

	public void carregarFotosCampoPendentes() {
		if (relatorio == null || relatorio.getId() == null) {
			return;
		}
		try {
			fotosCampoPendentes = fotoCampoService.buscarPendentesPorRelatorioId(relatorio.getId());
			// as ja colocadas nos topicos nesta edicao continuam fora da lista
			fotosCampoPendentes.removeIf(f -> fotosAlocadas.contains(f.getId()));
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao carregar fotos de campo do relatório id={}", relatorio.getId(), e);
			Mensagens.erro("Não foi possível carregar as fotos de campo deste relatório.");
		}
	}

	// =========================================================
	// TOPICOS E SUBTOPICOS
	// =========================================================

	public void adicionarNovoTopico() {
		topicosEdicao.add(new TopicoRelatorio());
	}

	public void removerTopico(int index) {
		if (index >= 0 && index < topicosEdicao.size()) {
			topicosEdicao.remove(index);
		}
	}

	public void adicionarSubtopico(int topicoIndex) {
		if (topicoIndex >= 0 && topicoIndex < topicosEdicao.size()) {
			topicosEdicao.get(topicoIndex).adicionarSubtopico(new SubtopicoRelatorio());
		}
	}

	public void removerSubtopico(int topicoIndex, int subIndex) {
		List<SubtopicoRelatorio> subs = subtopicos(topicoIndex);
		if (subs != null && subIndex >= 0 && subIndex < subs.size()) {
			subs.remove(subIndex);
		}
	}

	// =========================================================
	// IMAGENS DOS TOPICOS
	// =========================================================

	public void uploadImagemTopico() {
		if (topicoUploadIndex < 0 || topicoUploadIndex >= topicosEdicao.size()) {
			Mensagens.erro("Tópico inválido para o envio da imagem.");
			return;
		}
		String url = salvarImagem(imagemTopicoUpload);
		if (url != null) {
			topicosEdicao.get(topicoUploadIndex).adicionarImagem(url);
			imagemTopicoUpload = null;
			Mensagens.sucesso("Imagem adicionada ao tópico " + (topicoUploadIndex + 1) + ".");
		}
	}

	public void uploadImagemSubtopico() {
		List<SubtopicoRelatorio> subs = subtopicos(subtopicoUploadTopicoIndex);
		if (subs == null || subtopicoUploadIndex < 0 || subtopicoUploadIndex >= subs.size()) {
			Mensagens.erro("Subtópico inválido para o envio da imagem.");
			return;
		}
		String url = salvarImagem(imagemSubtopicoUpload);
		if (url != null) {
			subs.get(subtopicoUploadIndex).adicionarImagem(url);
			imagemSubtopicoUpload = null;
			Mensagens.sucesso("Imagem adicionada ao subtópico " + (subtopicoUploadTopicoIndex + 1) + "."
					+ (subtopicoUploadIndex + 1) + ".");
		}
	}

	/** Valida e grava a imagem; devolve a URL, ou null (com mensagem) se falhar. */
	private String salvarImagem(UploadedFile arquivo) {
		if (!ArquivosEnviados.selecionado(arquivo)) {
			Mensagens.erro("Escolha uma imagem antes de clicar em \"Adicionar Foto\".");
			return null;
		}
		try {
			String nome = ArquivosEnviados.salvar(arquivo, Uploads.RELATORIOS, null, true, Uploads.EXTENSOES_FOTO);
			return Uploads.url(Uploads.RELATORIOS, nome);
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Erro ao gravar imagem do relatório", e);
			Mensagens.erroInesperado();
		}
		return null;
	}

	public void removerImagemTopico(int topicoIndex, int imgIndex) {
		if (topicoIndex >= 0 && topicoIndex < topicosEdicao.size()) {
			topicosEdicao.get(topicoIndex).removerImagem(imgIndex);
		}
	}

	public void removerImagemSubtopico(int topicoIndex, int subIndex, int imgIndex) {
		List<SubtopicoRelatorio> subs = subtopicos(topicoIndex);
		if (subs != null && subIndex >= 0 && subIndex < subs.size()) {
			subs.get(subIndex).removerImagem(imgIndex);
		}
	}

	// =========================================================
	// FOTOS DE CAMPO
	// =========================================================

	/** Coloca a foto pendente no topico (ou subtopico) escolhido e a marca como utilizada. */
	public void alocarFotoNoTopico(Long fotoCampoId) {
		Integer ti = indice(fotoTopicoIndexMap.get(fotoCampoId));
		Integer si = indice(fotoSubtopicoIndexMap.get(fotoCampoId));
		if (ti == null) {
			Mensagens.erro("Selecione o tópico de destino da foto.");
			return;
		}
		if (ti >= topicosEdicao.size()) {
			Mensagens.erro("O tópico selecionado não existe mais. Escolha outro.");
			return;
		}
		List<SubtopicoRelatorio> subs = topicosEdicao.get(ti).getSubtopicos();
		if (si != null && si >= subs.size()) {
			Mensagens.erro("O subtópico selecionado não existe mais. Escolha outro.");
			return;
		}
		try {
			FotoCampo foto = fotoCampoService.buscarPorId(fotoCampoId);
			if (foto == null) {
				removerPendente(fotoCampoId);
				Mensagens.aviso("Esta foto não existe mais (pode ter sido descartada em outra tela).");
				return;
			}
			if (si != null) {
				subs.get(si).adicionarImagem(foto.getUrlFoto());
			} else {
				topicosEdicao.get(ti).adicionarImagem(foto.getUrlFoto());
			}
			fotosAlocadas.add(fotoCampoId);
			removerPendente(fotoCampoId);
			String destino = si != null ? "subtópico " + (ti + 1) + "." + (si + 1) : "tópico " + (ti + 1);
			Mensagens.sucesso("Foto colocada no " + destino + ". Salve o relatório para gravar a alteração.");
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao alocar foto de campo id={}", fotoCampoId, e);
			Mensagens.erroInesperado();
		}
	}

	public void descartarFotoCampo(Long fotoCampoId) {
		try {
			FotoCampo foto = fotoCampoService.buscarPorId(fotoCampoId);
			if (foto != null) {
				fotoCampoService.descartar(foto);
			}
			fotosAlocadas.remove(fotoCampoId);
			removerPendente(fotoCampoId);
			Mensagens.sucesso("Foto descartada.");
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao descartar foto de campo id={}", fotoCampoId, e);
			Mensagens.erroInesperado();
		}
	}

	public boolean isTemFotosCampoPendentes() {
		return !fotosCampoPendentes.isEmpty();
	}

	/** Ao trocar o topico de uma foto, limpa o subtopico escolhido (os indices mudam). */
	public void onTopicoFotoChange(Long fotoCampoId) {
		fotoSubtopicoIndexMap.remove(fotoCampoId);
	}

	public List<SelectItem> getOpcoesTopico() {
		List<SelectItem> itens = new ArrayList<>();
		for (int i = 0; i < topicosEdicao.size(); i++) {
			itens.add(new SelectItem(String.valueOf(i), Texto.rotuloNumerado("Tópico", i, topicosEdicao.get(i).getTitulo())));
		}
		return itens;
	}

	/** Subtopicos do topico escolhido para a foto (para o segundo select). */
	public List<SelectItem> getOpcoesSubtopicoParaFoto(Long fotoCampoId) {
		List<SelectItem> itens = new ArrayList<>();
		Integer ti = indice(fotoTopicoIndexMap.get(fotoCampoId));
		List<SubtopicoRelatorio> subs = ti != null ? subtopicos(ti) : null;
		if (subs != null) {
			for (int i = 0; i < subs.size(); i++) {
				itens.add(new SelectItem(String.valueOf(i), Texto.rotuloNumerado("Subtópico", i, subs.get(i).getTitulo())));
			}
		}
		return itens;
	}

	private void removerPendente(Long fotoCampoId) {
		fotosCampoPendentes.removeIf(f -> f.getId().equals(fotoCampoId));
		fotoTopicoIndexMap.remove(fotoCampoId);
		fotoSubtopicoIndexMap.remove(fotoCampoId);
	}

	/** Converte o valor do select em indice; null para vazio, "-1" ou invalido. */
	static Integer indice(String valor) {
		if (valor == null || valor.isEmpty() || NENHUM.equals(valor)) {
			return null;
		}
		try {
			int i = Integer.parseInt(valor);
			return i >= 0 ? i : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private List<SubtopicoRelatorio> subtopicos(int topicoIndex) {
		return topicoIndex >= 0 && topicoIndex < topicosEdicao.size() ? topicosEdicao.get(topicoIndex).getSubtopicos()
				: null;
	}

	// =========================================================
	// SALVAR
	// =========================================================

	public void salvar() {
		try {
			relatorio.setCliente(clienteSelecionadoId != null ? clienteService.buscarPorId(clienteSelecionadoId) : null);
			relatorioService.salvar(relatorio, topicosEdicao, SessaoUsuario.nomeOuPadrao("Sistema"), fotosAlocadas);
			Mensagens.sucesso(modoEdicao ? "Relatório atualizado. Para voltar a editá-lo, abra-o em Rascunhos."
					: "Relatório cadastrado. Ele já aparece em Rascunhos; o formulário foi limpo para um novo.");
			limpar();
		} catch (RegraNegocioException e) {
			Mensagens.erro(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.error("Erro ao salvar relatório", e);
			Mensagens.erroInesperado();
		}
	}

	public void limpar() {
		relatorio = new Relatorio();
		clienteSelecionadoId = null;
		topicosEdicao = new ArrayList<>();
		imagemTopicoUpload = null;
		imagemSubtopicoUpload = null;
		topicoUploadIndex = -1;
		subtopicoUploadTopicoIndex = -1;
		subtopicoUploadIndex = -1;
		modoEdicao = false;
		fotosCampoPendentes = new ArrayList<>();
		fotoTopicoIndexMap = new HashMap<>();
		fotoSubtopicoIndexMap = new HashMap<>();
		fotosAlocadas = new HashSet<>();
	}

	// =========================================================
	// GETTERS E SETTERS
	// =========================================================

	public Relatorio getRelatorio() {
		return relatorio;
	}

	public List<CadastroCliente> getClientesDisponiveis() {
		return clientesDisponiveis;
	}

	public Long getClienteSelecionadoId() {
		return clienteSelecionadoId;
	}

	public void setClienteSelecionadoId(Long id) {
		this.clienteSelecionadoId = id;
	}

	public List<TopicoRelatorio> getTopicosEdicao() {
		return topicosEdicao;
	}

	public UploadedFile getImagemTopicoUpload() {
		return imagemTopicoUpload;
	}

	public void setImagemTopicoUpload(UploadedFile f) {
		this.imagemTopicoUpload = f;
	}

	public int getTopicoUploadIndex() {
		return topicoUploadIndex;
	}

	public void setTopicoUploadIndex(int i) {
		this.topicoUploadIndex = i;
	}

	public UploadedFile getImagemSubtopicoUpload() {
		return imagemSubtopicoUpload;
	}

	public void setImagemSubtopicoUpload(UploadedFile f) {
		this.imagemSubtopicoUpload = f;
	}

	public int getSubtopicoUploadTopicoIndex() {
		return subtopicoUploadTopicoIndex;
	}

	public void setSubtopicoUploadTopicoIndex(int i) {
		this.subtopicoUploadTopicoIndex = i;
	}

	public int getSubtopicoUploadIndex() {
		return subtopicoUploadIndex;
	}

	public void setSubtopicoUploadIndex(int i) {
		this.subtopicoUploadIndex = i;
	}

	public boolean isModoEdicao() {
		return modoEdicao;
	}

	public List<FotoCampo> getFotosCampoPendentes() {
		return fotosCampoPendentes;
	}

	public Map<Long, String> getFotoTopicoIndexMap() {
		return fotoTopicoIndexMap;
	}

	public Map<Long, String> getFotoSubtopicoIndexMap() {
		return fotoSubtopicoIndexMap;
	}
}

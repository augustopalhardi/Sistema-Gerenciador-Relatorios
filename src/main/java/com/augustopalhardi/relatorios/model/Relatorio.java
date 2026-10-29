package com.augustopalhardi.relatorios.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Relatorio implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// === DADOS CLIENTE ===
	@ManyToOne
	@JoinColumn(name = "cliente_id")
	private CadastroCliente cliente;

	@Temporal(TemporalType.DATE)
	@Column(name = "data_inicio")
	private Date dataInicio;

	@Temporal(TemporalType.DATE)
	@Column(name = "data_termino")
	private Date dataTermino;

	@Column(name = "responsavel_tec_cliente", length = 200)
	private String responsavelTecCliente;

	@Column(name = "observacao", columnDefinition = "TEXT")
	private String observacao;

	// === DADOS RELATÓRIO ===
	@Column(name = "responsavel_tecnico", length = 200)
	private String responsavelTecnico;

	@Column(name = "titulo", length = 300)
	private String titulo;

	@Column(name = "apresentacao", columnDefinition = "TEXT")
	private String apresentacao;

	@Column(name = "escopo", columnDefinition = "TEXT")
	private String escopo;

	@Column(name = "introducao", columnDefinition = "TEXT")
	private String introducao;

	@Column(name = "conclusao", columnDefinition = "TEXT")
	private String conclusao;

	@Column(name = "recomendacoes", columnDefinition = "TEXT")
	private String recomendacoes;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_criacao")
	private Date dataCriacao;

	@Column(name = "usuario_registro", length = 100)
	private String usuarioRegistro;

	// === TÓPICOS ===
	@OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("numero ASC")
	private List<TopicoRelatorio> topicos = new ArrayList<>();

	public Relatorio() {
	}

	public void adicionarTopico(TopicoRelatorio topico) {
		topico.setRelatorio(this);
		this.topicos.add(topico);
	}

	/**
	 * Substitui os topicos pelos da lista, na ordem dada: numera topicos e
	 * subtopicos (1, 2, 3...) e liga cada um ao seu pai.
	 *
	 * Usa clear() + addAll() na propria lista mapeada para que o JPA detecte as
	 * remocoes (orphanRemoval) e as inclusoes numa unica operacao.
	 */
	public void substituirTopicos(List<TopicoRelatorio> novos) {
		List<TopicoRelatorio> lista = new ArrayList<>(novos);
		for (int i = 0; i < lista.size(); i++) {
			TopicoRelatorio t = lista.get(i);
			t.setNumero(i + 1);
			t.setRelatorio(this);
			t.renumerarSubtopicos();
		}
		topicos.clear();
		topicos.addAll(lista);
	}

	/**
	 * Cria uma copia para edicao: mesmos dados e topicos, titulo com "(Cópia)",
	 * data de criacao e usuario de registro novos. A copia nao tem ID.
	 */
	public Relatorio duplicar(String usuarioRegistro, Date agora) {
		Relatorio copia = new Relatorio();
		copia.setCliente(cliente);
		copia.setDataInicio(dataInicio);
		copia.setDataTermino(dataTermino);
		copia.setResponsavelTecCliente(responsavelTecCliente);
		copia.setObservacao(observacao);
		copia.setResponsavelTecnico(responsavelTecnico);
		copia.setTitulo((titulo != null ? titulo : "") + " (Cópia)");
		copia.setApresentacao(apresentacao);
		copia.setEscopo(escopo);
		copia.setIntroducao(introducao);
		copia.setConclusao(conclusao);
		copia.setRecomendacoes(recomendacoes);
		copia.setDataCriacao(agora);
		copia.setUsuarioRegistro(usuarioRegistro != null ? usuarioRegistro : this.usuarioRegistro);
		for (TopicoRelatorio t : topicos) {
			copia.adicionarTopico(t.copiar());
		}
		return copia;
	}

	/**
	 * Valida os dados minimos para salvar. Devolve a mensagem de erro para o
	 * usuario, ou null se estiver tudo certo.
	 */
	public String validar() {
		if (cliente == null) {
			return "Selecione um cliente.";
		}
		if (titulo == null || titulo.trim().isEmpty()) {
			return "Informe o título do relatório.";
		}
		if (dataInicio != null && dataTermino != null && dataTermino.before(dataInicio)) {
			return "A data de término não pode ser anterior à data de início.";
		}
		return null;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CadastroCliente getCliente() {
		return cliente;
	}

	public void setCliente(CadastroCliente cliente) {
		this.cliente = cliente;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataTermino() {
		return dataTermino;
	}

	public void setDataTermino(Date dataTermino) {
		this.dataTermino = dataTermino;
	}

	public String getResponsavelTecCliente() {
		return responsavelTecCliente;
	}

	public void setResponsavelTecCliente(String responsavelTecCliente) {
		this.responsavelTecCliente = responsavelTecCliente;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getResponsavelTecnico() {
		return responsavelTecnico;
	}

	public void setResponsavelTecnico(String responsavelTecnico) {
		this.responsavelTecnico = responsavelTecnico;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getApresentacao() {
		return apresentacao;
	}

	public void setApresentacao(String apresentacao) {
		this.apresentacao = apresentacao;
	}

	public String getEscopo() {
		return escopo;
	}

	public void setEscopo(String escopo) {
		this.escopo = escopo;
	}

	public String getIntroducao() {
		return introducao;
	}

	public void setIntroducao(String introducao) {
		this.introducao = introducao;
	}

	public String getConclusao() {
		return conclusao;
	}

	public void setConclusao(String conclusao) {
		this.conclusao = conclusao;
	}

	public String getRecomendacoes() {
		return recomendacoes;
	}

	public void setRecomendacoes(String recomendacoes) {
		this.recomendacoes = recomendacoes;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public List<TopicoRelatorio> getTopicos() {
		return topicos;
	}

	public void setTopicos(List<TopicoRelatorio> topicos) {
		this.topicos = topicos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		Relatorio other = (Relatorio) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}

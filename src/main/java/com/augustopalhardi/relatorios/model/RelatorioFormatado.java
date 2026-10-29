package com.augustopalhardi.relatorios.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "relatorio_formatado")
public class RelatorioFormatado implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Relatorio de origem (para referência e exibição do título/cliente)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relatorio_id")
	private Relatorio relatorio;

	// Título customizado do rascunho formatado (pode ser diferente do relatorio)
	@Column(name = "titulo_rascunho", length = 400)
	private String tituloRascunho;

	// HTML completo do editor formatado (pode ser muito grande)
	@Column(name = "conteudo_html", columnDefinition = "LONGTEXT")
	private String conteudoHtml;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_criacao")
	private Date dataCriacao;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_atualizacao")
	private Date dataAtualizacao;

	@Column(name = "usuario_registro", length = 100)
	private String usuarioRegistro;

	public RelatorioFormatado() {
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Relatorio getRelatorio() {
		return relatorio;
	}

	public void setRelatorio(Relatorio r) {
		this.relatorio = r;
	}

	public String getTituloRascunho() {
		return tituloRascunho;
	}

	public void setTituloRascunho(String t) {
		this.tituloRascunho = t;
	}

	public String getConteudoHtml() {
		return conteudoHtml;
	}

	public void setConteudoHtml(String h) {
		this.conteudoHtml = h;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date d) {
		this.dataCriacao = d;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date d) {
		this.dataAtualizacao = d;
	}

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String u) {
		this.usuarioRegistro = u;
	}
}

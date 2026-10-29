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

/**
 * Foto tirada em campo e vinculada a um relatório. Fica "pendente" até o
 * técnico alocá-la a um tópico/subtópico.
 */
@Entity
@Table(name = "foto_campo")
public class FotoCampo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Relatório ao qual esta foto pertence */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relatorio_id", nullable = false)
	private Relatorio relatorio;

	/** Usuário que fez o upload */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "usuario_id")
	private CadastroUsuario usuario;

	/** Caminho/URL do arquivo salvo em disco */
	@Column(name = "url_foto", length = 500, nullable = false)
	private String urlFoto;

	/** Legenda opcional inserida pelo técnico no campo */
	@Column(name = "legenda", length = 500)
	private String legenda;

	/** Data/hora do upload */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_upload")
	private Date dataUpload;

	/**
	 * Indica se a foto já foi vinculada a algum tópico/subtópico. false = pendente
	 * (aparece no painel de fotos do campo) true = já utilizada
	 */
	@Column(name = "utilizada")
	private boolean utilizada = false;

	public FotoCampo() {
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

	public void setRelatorio(Relatorio relatorio) {
		this.relatorio = relatorio;
	}

	public CadastroUsuario getUsuario() {
		return usuario;
	}

	public void setUsuario(CadastroUsuario usuario) {
		this.usuario = usuario;
	}

	public String getUrlFoto() {
		return urlFoto;
	}

	public void setUrlFoto(String urlFoto) {
		this.urlFoto = urlFoto;
	}

	public String getLegenda() {
		return legenda;
	}

	public void setLegenda(String legenda) {
		this.legenda = legenda;
	}

	public Date getDataUpload() {
		return dataUpload;
	}

	public void setDataUpload(Date dataUpload) {
		this.dataUpload = dataUpload;
	}

	public boolean isUtilizada() {
		return utilizada;
	}

	public void setUtilizada(boolean utilizada) {
		this.utilizada = utilizada;
	}
}

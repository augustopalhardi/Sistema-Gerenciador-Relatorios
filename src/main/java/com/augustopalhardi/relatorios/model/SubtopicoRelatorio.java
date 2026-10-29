package com.augustopalhardi.relatorios.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class SubtopicoRelatorio implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "numero")
	private int numero;

	@Column(name = "titulo", length = 300)
	private String titulo;

	@Column(name = "conteudo", columnDefinition = "LONGTEXT")
	private String conteudo;

	// URLs das imagens separadas por ;
	@Column(name = "imagens", columnDefinition = "LONGTEXT")
	private String imagens;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "topico_id")
	private TopicoRelatorio topico;

	public SubtopicoRelatorio() {
	}

	@Transient
	public List<String> getListaImagens() {
		return ListaImagens.lista(imagens);
	}

	public void adicionarImagem(String url) {
		imagens = ListaImagens.adicionar(imagens, url);
	}

	public void removerImagem(int index) {
		imagens = ListaImagens.remover(imagens, index);
	}

	/** Copia titulo, conteudo e imagens (sem ID nem topico). */
	public SubtopicoRelatorio copiar() {
		SubtopicoRelatorio copia = new SubtopicoRelatorio();
		copia.setNumero(numero);
		copia.setTitulo(titulo);
		copia.setConteudo(conteudo);
		copia.setImagens(imagens);
		return copia;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public String getImagens() {
		return imagens;
	}

	public void setImagens(String imagens) {
		this.imagens = imagens;
	}

	public TopicoRelatorio getTopico() {
		return topico;
	}

	public void setTopico(TopicoRelatorio topico) {
		this.topico = topico;
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
		SubtopicoRelatorio other = (SubtopicoRelatorio) obj;
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

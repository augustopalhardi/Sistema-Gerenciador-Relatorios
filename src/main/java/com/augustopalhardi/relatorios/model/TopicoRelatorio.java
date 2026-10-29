package com.augustopalhardi.relatorios.model;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.persistence.Transient;

@Entity
public class TopicoRelatorio implements Serializable {

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
	@JoinColumn(name = "relatorio_id")
	private Relatorio relatorio;

	@OneToMany(mappedBy = "topico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("numero ASC")
	private List<SubtopicoRelatorio> subtopicos = new ArrayList<>();

	public TopicoRelatorio() {
	}

	public void adicionarSubtopico(SubtopicoRelatorio subtopico) {
		subtopico.setTopico(this);
		this.subtopicos.add(subtopico);
	}

	public void removerSubtopico(SubtopicoRelatorio subtopico) {
		subtopico.setTopico(null);
		this.subtopicos.remove(subtopico);
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

	/** Copia titulo, conteudo, imagens e subtopicos (sem IDs), para duplicar um relatorio. */
	public TopicoRelatorio copiar() {
		TopicoRelatorio copia = new TopicoRelatorio();
		copia.setNumero(numero);
		copia.setTitulo(titulo);
		copia.setConteudo(conteudo);
		copia.setImagens(imagens);
		for (SubtopicoRelatorio sub : subtopicos) {
			copia.adicionarSubtopico(sub.copiar());
		}
		return copia;
	}

	/** Numera os subtopicos (1, 2, 3...) na ordem da lista e liga cada um a este topico. */
	public void renumerarSubtopicos() {
		for (int i = 0; i < subtopicos.size(); i++) {
			subtopicos.get(i).setNumero(i + 1);
			subtopicos.get(i).setTopico(this);
		}
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

	public Relatorio getRelatorio() {
		return relatorio;
	}

	public void setRelatorio(Relatorio relatorio) {
		this.relatorio = relatorio;
	}

	public List<SubtopicoRelatorio> getSubtopicos() {
		return subtopicos;
	}

	public void setSubtopicos(List<SubtopicoRelatorio> subtopicos) {
		this.subtopicos = subtopicos;
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
		TopicoRelatorio other = (TopicoRelatorio) obj;
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

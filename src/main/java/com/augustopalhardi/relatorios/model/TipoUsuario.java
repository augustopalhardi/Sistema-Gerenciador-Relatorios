package com.augustopalhardi.relatorios.model;

public enum TipoUsuario {
	ADMINISTRADOR("Administrador"), FUNCIONARIO("Funcionário"), TECNICO("Técnico");

	private String descricao;

	TipoUsuario(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}

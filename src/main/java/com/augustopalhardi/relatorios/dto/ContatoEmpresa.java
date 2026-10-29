package com.augustopalhardi.relatorios.dto;

import com.augustopalhardi.relatorios.model.DadosEmpresa;
import com.augustopalhardi.relatorios.util.Texto;

/**
 * Dados da empresa prestadora ja tratados para exibicao no documento (sem
 * nulls), com as linhas de contato montadas.
 */
public record ContatoEmpresa(String nome, String telefone, String emailContato, String emailComercial, String site,
		String arquivoLogo) {

	/** Converte a entidade; aceita null (devolve tudo vazio). */
	public static ContatoEmpresa de(DadosEmpresa dados) {
		if (dados == null) {
			return new ContatoEmpresa("", "", "", "", "", "");
		}
		return new ContatoEmpresa(Texto.nvl(dados.getNomeEmpresa()), Texto.nvl(dados.getTelefonePrincipal()),
				Texto.nvl(dados.getEmailContato()), Texto.nvl(dados.getEmailComercial()), Texto.nvl(dados.getSite()),
				Texto.nvl(dados.getUrlLogo()));
	}

	/** Linha do rodape e da identificacao: telefone | e-mail de contato | site. */
	public String linhaContato() {
		return juntar(telefone, emailContato, site);
	}

	/**
	 * Linha da assinatura: telefone | e-mail comercial (ou o de contato, se nao
	 * houver comercial).
	 */
	public String linhaAssinatura() {
		return juntar(telefone, emailComercial.isEmpty() ? emailContato : emailComercial);
	}

	public boolean temLogo() {
		return !arquivoLogo.isEmpty();
	}

	private static String juntar(String... partes) {
		StringBuilder sb = new StringBuilder();
		for (String p : partes) {
			if (p == null || p.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(p);
		}
		return sb.toString();
	}
}

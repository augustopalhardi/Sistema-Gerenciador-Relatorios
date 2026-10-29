package com.augustopalhardi.relatorios.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Le a configuracao de um arquivo externo ao projeto, para que credenciais e
 * caminhos especificos da instalacao nao sejam versionados nem empacotados
 * dentro do .war.
 *
 * O caminho do arquivo e informado pela system property
 * "relatorios.config.file". Uma chave propria (em vez de algo generico como
 * "config.file") evita conflito quando outra aplicacao roda na mesma JVM do
 * Tomcat: as system properties sao compartilhadas entre as aplicacoes.
 *
 * Exemplo: -Drelatorios.config.file=/caminho/para/config.properties
 * Padrao (sem a property): /etc/relatorios/config.properties
 *
 * Modelo das chaves: config.properties.example, na raiz do com.augustopalhardi.relatorios.
 */
public class Config {

	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
	private static final Properties props = new Properties();

	static {
		String caminho = System.getProperty("relatorios.config.file", "/etc/relatorios/config.properties");
		try (InputStream in = new FileInputStream(caminho)) {
			props.load(in);
			LOGGER.info("Configuracao carregada de: {}", caminho);
		} catch (Exception e) {
			LOGGER.error("Falha ao carregar configuracao de: {}", caminho, e);
			throw new IllegalStateException("Arquivo de configuracao nao encontrado: " + caminho, e);
		}
	}

	/** Valor obrigatorio: lanca excecao se a chave estiver ausente ou vazia. */
	public static String get(String chave) {
		String valor = props.getProperty(chave);
		if (valor == null || valor.trim().isEmpty()) {
			throw new IllegalStateException("Configuracao ausente: " + chave);
		}
		return valor.trim();
	}

	/** Valor opcional: devolve o padrao se a chave estiver ausente ou vazia. */
	public static String getOuPadrao(String chave, String padrao) {
		String valor = props.getProperty(chave);
		return (valor == null || valor.trim().isEmpty()) ? padrao : valor.trim();
	}

	private Config() {
	}
}

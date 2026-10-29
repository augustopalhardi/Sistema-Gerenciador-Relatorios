package com.augustopalhardi.relatorios.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Usa a configuracao falsa de src/test/resources/config-teste.properties
 * (upload.dir=target/uploads-teste), apontada pelo surefire.
 */
class UploadsTest {

	@Test
	void diretorioBaseVemDaConfiguracao() {
		assertEquals(Paths.get("target/uploads-teste").toAbsolutePath().normalize(), Uploads.diretorioBase());
	}

	@Test
	void nomeSemCaminhoDeWindowsEUnix() {
		assertEquals("foto.jpg", Uploads.nomeSemCaminho("C:\\Users\\tecnico\\foto.jpg"));
		assertEquals("foto.jpg", Uploads.nomeSemCaminho("/home/tecnico/foto.jpg"));
		assertEquals("foto.jpg", Uploads.nomeSemCaminho("foto.jpg"));
		assertEquals("", Uploads.nomeSemCaminho(null));
	}

	@Test
	void extensaoEmMinusculas() {
		assertEquals("jpeg", Uploads.extensao("FOTO.JPEG"));
		assertEquals("", Uploads.extensao("semextensao"));
		assertEquals("", Uploads.extensao(".oculto"));
		assertEquals("", Uploads.extensao("termina."));
	}

	@Test
	void gerarNomeLimpaCaracteresEMantemExtensao() {
		assertEquals("campo_1000_Painel_el_trico_bloco_B.jpg",
				Uploads.gerarNome("campo", "C:\\fotos\\Painel elétrico bloco B.JPG", true, 1000));
		assertEquals("cliente_1000.png", Uploads.gerarNome("cliente", "logo.png", false, 1000));
		assertEquals("1000_a.gif", Uploads.gerarNome(null, "a.gif", true, 1000));
	}

	@Test
	void gerarNomeNaoPermiteSubirDiretorio() {
		String nome = Uploads.gerarNome("x", "../../etc/passwd", true, 1);
		assertFalse(nome.contains("/"));
		assertFalse(nome.contains(".."));
	}

	@Test
	void validarFormatoETamanho() {
		assertNull(Uploads.validar("foto.jpg", 1024, Uploads.EXTENSOES_FOTO));
		assertEquals("Selecione um arquivo.", Uploads.validar("", 10, Uploads.EXTENSOES_FOTO));
		assertTrue(Uploads.validar("script.exe", 10, Uploads.EXTENSOES_FOTO).startsWith("Formato não aceito"));
		assertTrue(Uploads.validar("logo.svg", 10, Uploads.EXTENSOES_FOTO).startsWith("Formato não aceito"));
		assertNull(Uploads.validar("logo.svg", 10, Uploads.EXTENSOES_LOGO));
		assertEquals("O arquivo enviado está vazio.", Uploads.validar("foto.jpg", 0, Uploads.EXTENSOES_FOTO));
		assertEquals("O arquivo deve ter no máximo 5 MB.",
				Uploads.validar("foto.jpg", Uploads.TAMANHO_MAXIMO + 1, Uploads.EXTENSOES_FOTO));
	}

	@Test
	void caminhoQueSaiDoDiretorioBaseEhRecusado() {
		assertNull(Uploads.resolverDentroDoBase("../config.properties"));
		assertNull(Uploads.resolverDentroDoBase("clientes/../../fora.txt"));
		assertNull(Uploads.resolverDentroDoBase(""));
		assertNotNull(Uploads.resolverDentroDoBase("clientes/logo.png"));
		assertNull(Uploads.arquivoDaUrl("/outra/coisa.png"));
		assertNull(Uploads.arquivoDaUrl("/uploads/../../segredo"));
	}

	@Test
	void urlEArquivoSaoInversos() {
		String url = Uploads.url(Uploads.USUARIOS, "usuario_1.png");
		assertEquals("/uploads/usuarios/usuario_1.png", url);
		assertEquals(Uploads.diretorioBase().resolve("usuarios/usuario_1.png").toFile(), Uploads.arquivoDaUrl(url));
	}

	@Test
	void salvarEExcluirArquivo() throws Exception {
		File gravado = Uploads.salvar(new ByteArrayInputStream("conteudo".getBytes(StandardCharsets.UTF_8)),
				Uploads.RELATORIOS, "teste_salvar.txt");
		assertTrue(gravado.isFile());
		assertEquals("conteudo", new String(Files.readAllBytes(gravado.toPath()), StandardCharsets.UTF_8));

		assertTrue(Uploads.excluirPorUrl(Uploads.url(Uploads.RELATORIOS, "teste_salvar.txt")));
		assertFalse(gravado.exists());
		assertFalse(Uploads.excluir(Uploads.RELATORIOS, "teste_salvar.txt"));
		assertFalse(Uploads.excluir(Uploads.RELATORIOS, null));
	}

	@Test
	void tipoDeConteudoESvgComoDownload() {
		assertEquals("image/jpeg", Uploads.tipoConteudo("a.JPG"));
		assertEquals("image/svg+xml", Uploads.tipoConteudo("logo.svg"));
		assertEquals("application/octet-stream", Uploads.tipoConteudo("a.exe"));
		assertTrue(Uploads.exibivelNoNavegador("image/png"));
		assertFalse(Uploads.exibivelNoNavegador("image/svg+xml"));
	}
}

<p align="center">
  <img src="docs/logo.svg" alt="Sistema Gerenciador de Relatórios" width="96" />
</p>

<h1 align="center">Sistema Gerenciador de Relatórios</h1>

<p align="center">
  Sistema web para produção de relatórios técnicos de atendimento:<br/>
  o técnico registra o conteúdo e as fotos de campo, o sistema monta o documento pronto para PDF.
</p>

<p align="center">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-orange" />
  <img alt="JSF 2.3" src="https://img.shields.io/badge/JSF-2.3-blue" />
  <img alt="PrimeFaces 12" src="https://img.shields.io/badge/PrimeFaces-12-blue" />
  <img alt="Hibernate 5.6" src="https://img.shields.io/badge/Hibernate-5.6-59666C" />
  <img alt="MySQL 8" src="https://img.shields.io/badge/MySQL-8-4479A1" />
  <img alt="JUnit 5" src="https://img.shields.io/badge/JUnit-5-25A162" />
  <img alt="Licença MIT" src="https://img.shields.io/badge/licen%C3%A7a-MIT-green" />
</p>

---

## Sobre

Desenvolvido como projeto freelance para uma empresa do setor de manutenção industrial. Esta é a versão pública do código, sem dados, marca ou infraestrutura do cliente.

**O problema:** os Relatórios Técnicos de Atividades (RTA) eram montados manualmente no Word: copiar fotos do celular, redimensionar uma a uma, escrever legendas, numerar tópicos e refazer o sumário sempre que algo mudava de lugar. Cada relatório levava horas de diagramação, um trabalho que não tem nada de técnico e que erra fácil (numeração fora de ordem, foto sem legenda, sumário desatualizado).

**A solução:** separar as duas coisas. **O técnico escreve o conteúdo, o sistema cuida da forma.**

Em números: cerca de 5,4 mil linhas de Java em 52 classes, um editor visual em JavaScript puro (~900 linhas), 10 telas JSF e 64 testes automatizados.

---

## Funcionalidades

### Redação do relatório
- Cliente, responsáveis técnicos (do cliente e da empresa prestadora), local e datas
- Apresentação, escopo, introdução, conclusão e recomendações
- Corpo organizado em **tópicos e subtópicos**, cada um com texto rico (editor Quill) e imagens próprias
- Duplicação de relatórios para reaproveitar a estrutura de um atendimento parecido

### Fotos de campo
Fotos tiradas durante o atendimento são enviadas e ficam **pendentes**, vinculadas ao relatório. Na hora de escrever, aparecem num painel lateral e o técnico as **aloca** ao tópico ou subtópico correspondente; ao salvar, a foto é marcada como utilizada e sai da lista. Acaba a dúvida de "de qual atendimento era mesmo essa foto?".

### Formatação automática
A tela **Verificar Relatório** transforma o conteúdo estruturado num documento pronto:
- **Capa** com o logo da empresa, o logo do cliente e o ano
- **Identificação** com cliente, local, responsáveis e datas
- **Sumário numerado automaticamente** pela hierarquia dos tópicos (4.1, 4.1.1, 4.1.2…)
- **Seções 1 a 6:** Apresentação, Escopo, Introdução, Descrição das Atividades, Conclusão e Recomendações
- **Grid de fotos** com legendas numeradas em sequência contínua (Foto 1, Foto 2…)
- **Assinatura** do responsável técnico e **rodapé** com os contatos da empresa

### Ajuste visual
O documento gerado abre num editor para refinar o que a automação não tem como adivinhar:
- **Fotos:** arrastar para reordenar, redimensionar, rotacionar, cortar e excluir
- **Marcações sobre as fotos:** setas, círculos, elipses e retângulos em SVG, com cor configurável
- **Layout:** número de colunas do grid de fotos
- **Texto:** negrito, itálico, sublinhado, tamanho e cor
- **Desfazer/Refazer** com Ctrl+Z e Ctrl+Y

O resultado é salvo como **rascunho formatado** e pode ser reaberto depois. A impressão abre o documento numa janela limpa, com folha de estilo própria e quebras de página nos pontos certos, e o PDF sai pelo próprio navegador.

### Cadastros
- **Clientes**, com logo usado na capa
- **Usuários**, com senha em hash BCrypt e foto de perfil
- **Dados da empresa** (logo, telefone, e-mails e site), que alimentam capa, rodapé e assinatura de todos os relatórios sem precisar recompilar nada

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Web / View | JSF 2.3 (Mojarra) + PrimeFaces 12 |
| Injeção de dependências | CDI 2.0 (Weld 3.1) |
| Persistência | JPA 2.2 + Hibernate 5.6 |
| Pool de conexões | c3p0 |
| Banco de dados | MySQL 8 |
| Segurança | jBCrypt · OWASP Java HTML Sanitizer |
| Editor visual | JavaScript puro (sem framework) |
| Build | Maven (`.war`) |
| Servidor | Apache Tomcat 9 |
| Log | SLF4J + Log4j 2 |
| Testes | JUnit 5 |

---

## Arquitetura

Arquitetura em camadas, com injeção de dependências via CDI. Os beans JSF só cuidam de entrada e mensagens; as regras ficam nos serviços e em funções puras, que são as partes cobertas por testes.

```
src/main/java/com/augustopalhardi/relatorios/
├── model/          Entidades JPA e regras do próprio domínio (validar, duplicar, numerar tópicos)
├── dao/            Acesso a dados (consultas JPQL)
├── dto/            Dados já tratados para a saída (contato da empresa no documento)
├── service/        Regras de negócio e transações
│   └── documento/  Montagem do documento formatado (função pura)
├── view/           Managed beans do JSF, mensagens padronizadas, sessão e upload
├── filter/         Autenticação e regras de acesso por URL
├── servlet/        Entrega dos arquivos enviados (fora do .war)
└── util/           Configuração externa, uploads, texto, senhas, sanitização de HTML
    └── jpa/        Produtor de EntityManager e interceptor transacional

src/main/webapp/
├── index.xhtml                Login e criação da primeira conta
├── home/                      Painel inicial
├── relatorio/                 Redação, rascunhos, rascunhos formatados e editor visual
├── cliente/, usuario/         Cadastros
├── empresa/, conta/           Dados da empresa e minha conta
├── error/                     Páginas 403, 404, 500 e sessão expirada
├── WEB-INF/templates/         Layout único das telas (mensagens, confirmação, erros AJAX)
├── WEB-INF/includes/          Cabeçalhos parametrizados
└── resources/                 CSS (com Tema.css centralizando as cores), JS do editor e imagens

src/test/java/                 Testes JUnit 5 (documento, regras de domínio, uploads, senhas, HTML)
```

### Modelo de domínio

```
Relatorio ──┬── TopicoRelatorio ── SubtopicoRelatorio
            ├── FotoCampo          (pendentes até serem alocadas)
            ├── CadastroCliente
            └── RelatorioFormatado (documento montado, HTML)
```

A separação entre `Relatorio` e `RelatorioFormatado` é deliberada: o primeiro guarda o **conteúdo estruturado** (fonte da verdade, reaproveitável), o segundo guarda o **documento diagramado** com os ajustes manuais. Reformatar um relatório não destrói os ajustes anteriores, gera outro rascunho.

### Decisões técnicas

**Documento gerado por função pura.** A montagem do HTML (capa, sumário, numeração, grid de fotos) fica na classe `GeradorDocumento`, sem acesso a banco nem a JSF. Recebe o relatório e devolve o documento, o que permite testar numeração, escape e ordem das fotos sem subir servidor.

**Marcações posicionadas em porcentagem na impressão.** As setas e círculos sobre as fotos são posicionados em pixels durante a edição. Impressos assim, sairiam do lugar, porque a foto muda de tamanho na página. Antes de imprimir, o sistema clona o documento e converte cada posição de `px` para `%` relativo à foto, e a marcação acompanha a imagem em qualquer escala.

**Edição de imagem destrutiva via canvas.** Corte e rotação não são aplicados por CSS: a imagem é redesenhada num `<canvas>` e o resultado substitui o original. O que se vê na tela é exatamente o que sai no PDF.

**Desfazer/Refazer próprio.** O `contenteditable` do navegador não tem histórico confiável quando o conteúdo é manipulado por script (arrastar fotos, inserir formas). O editor mantém a própria pilha de estados, com limite de 60 níveis e captura com *debounce* na digitação.

**Consulta direta para evitar o cache de primeiro nível.** Os tópicos são buscados por query explícita em vez de navegação pela relação da entidade. Sem isso, o Hibernate devolvia instâncias do cache com estado desatualizado durante a formatação, e o documento saía com conteúdo antigo.

**Sanitização do HTML rico.** O conteúdo dos tópicos vem de um editor rico e passa pelo OWASP Java HTML Sanitizer ao ser gravado e de novo ao entrar no documento. A política mantém a formatação do editor e remove scripts, handlers de evento e URLs `javascript:`. Os demais textos são escapados.

**Credenciais fora do código, com propriedade dedicada.** Nenhuma senha existe no repositório. A classe `Config` lê um arquivo externo apontado por **`relatorios.config.file`**, e não por um genérico `config.file`: aplicações no mesmo Tomcat compartilham as system properties da JVM e, com a mesma chave, uma conectaria no banco da outra.

**Uploads fora da aplicação.** Logos e fotos ficam num diretório externo e são servidos por um servlet que exige sessão, recusa caminhos fora do diretório (path traversal) e entrega SVG apenas como download. Formato e tamanho são validados no servidor, e um novo deploy não apaga os arquivos.

**Sessão protegida.** Senhas com BCrypt, troca do ID de sessão no login (contra *session fixation*), cookie `HttpOnly` e autorização por allow-list de páginas, com o padrão de negar.

**Uma tela, um layout.** Todas as telas usam um único template Facelets, que concentra mensagens (sucesso, aviso e erro no mesmo padrão), confirmação de ações destrutivas e tratamento de erro em requisições AJAX, incluindo sessão expirada.

**Tema de cores centralizado.** As cores vivem em variáveis CSS no `Tema.css`, em vez de códigos hexadecimais espalhados pelas folhas de estilo.

---

## Como executar

**Pré-requisitos:** JDK 17, Maven 3.8+, MySQL 8 e Apache Tomcat 9.

**1. Banco de dados.** Crie um banco vazio. As tabelas são criadas pelo Hibernate no primeiro start (`hbm2ddl.auto=update`).

```sql
CREATE DATABASE gerenciador_relatorios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**2. Configuração.** Copie [`config.properties.example`](config.properties.example) para um local **fora do projeto**, renomeie para `config.properties` e preencha o banco e, se quiser, a pasta de uploads (padrão: `<home do usuário>/relatorios-uploads`).

**3. Aponte a JVM do Tomcat para o arquivo:**

```
-Drelatorios.config.file=/caminho/para/config.properties
```

Sem o parâmetro, o caminho padrão é `/etc/relatorios/config.properties`.

> No Tomcat do Eclipse, o campo *VM arguments* pode não persistir, porque o Eclipse regenera a launch configuration. A alternativa que funciona é adicionar a linha `relatorios.config.file=...` ao final do `catalina.properties` do servidor: o Tomcat converte as entradas desse arquivo em system properties no boot.

**4. Build e deploy:**

```bash
mvn clean package
```

Copie `target/sistema-gerenciador-relatorios.war` para a pasta `webapps/` do Tomcat. A aplicação responde em `http://localhost:8080/sistema-gerenciador-relatorios`.

O `web.xml` vem com `PROJECT_STAGE=Production`. Para desenvolver, troque para `Development` (mensagens de erro detalhadas do JSF).

**5. Testes:**

```bash
mvn test
```

Os testes usam um `config.properties` fictício em `src/test/resources` e não precisam de banco de dados.

**6. Primeiro acesso.** Com o banco vazio, a tela de login mostra o botão **CRIAR CONTA**, que cadastra o primeiro usuário.

---

## Limitações conhecidas

- Não há perfis de acesso: todo usuário autenticado pode usar todas as telas, inclusive o cadastro de usuários.
- O vínculo entre relatório e quem o registrou é pelo nome do usuário; renomear um usuário desfaz esse vínculo.
- O HTML do documento formatado é salvo como o editor do navegador o produz (o conteúdo vindo dos tópicos já entra sanitizado).
- Imagens removidas de um tópico continuam no diretório de uploads.
- Não há limite de tentativas de login; em produção, use HTTPS e um proxy com rate limit.

---

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE).

## Autor

**Augusto de Barros Palhardi**
Desenvolvedor Java · [github.com/augustopalhardi](https://github.com/augustopalhardi)

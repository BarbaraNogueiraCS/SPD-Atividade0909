# Biblioteca — Atividade0909

Implementação da camada de persistência de uma biblioteca em **Java 17+, Jakarta Persistence (JPA), Hibernate e H2**, na branch `develop`. O domínio contempla leitores, carteirinhas, livros, autores, exemplares e empréstimos. A versão ORMLite/SQLite permanece no histórico da branch `main`.

## Artefatos da atividade

| Entrega | Arquivos |
|---|---|
| Diagrama de classes | [PlantUML](diagramas/classes.puml) e [PNG](diagramas/classes.png) |
| Diagrama E-R | [PlantUML](diagramas/er.puml) e [PNG](diagramas/er.png) |
| Camada de persistência | [Classes Java](src/main/java/br/edu/biblioteca), [esquema H2](src/main/resources/schema.sql), [configuração JPA](src/main/resources/META-INF/persistence.xml) |
| Testes interativos | [Notebook Java](notebooks/testes_persistencia.ipynb) |
| Testes automatizados | [Integração JUnit](src/test/java/br/edu/biblioteca/PersistenciaTest.java) |

## Modelagem e regras

- **Leitor–Carteirinha (1:1):** o leitor pode aguardar a emissão, mas tem no máximo uma carteirinha. Cada carteirinha tem um leitor obrigatório, com FK única e `@OneToOne`.
- **Livro–Exemplar (1:N):** um livro representa uma obra/edição; um exemplar representa uma cópia física de código único. `Exemplar.livro` usa `@ManyToOne` e `Livro.exemplares` usa `@OneToMany(mappedBy = "livro")`.
- **Livro–Autor (N:M):** representada pela entidade associativa `LivroAutor`, com duas relações `@ManyToOne` e unicidade do par `(livro_id, autor_id)`. A associação mantém ID próprio. Não se usa `@ManyToMany` porque o vínculo foi modelado como entidade explícita.
- **Leitor–Empréstimo e Exemplar–Empréstimo (1:N):** cada empréstimo corresponde a um leitor e um exemplar. Três cópias retiradas geram três empréstimos.
- Um exemplar pode ter diversos empréstimos históricos, mas somente um ativo. Devolver preenche a data sem apagar o histórico.
- Prazo e devolução não podem ser anteriores à retirada. Datas usam `LocalDate` em Java e `DATE` no H2.
- E-mail, ISBN, número da carteirinha e código do exemplar são únicos. A validação de formato de e-mail e ISBN não faz parte deste recorte; os exemplos do notebook são fictícios.
- A exclusão de registros referenciados é bloqueada. Não há remoção em cascata do histórico.

### Diagrama de classes

![Diagrama de classes JPA](diagramas/classes.png)

### Diagrama E-R

![Diagrama E-R H2](diagramas/er.png)

## Pilha tecnológica

| Tecnologia | Função |
|---|---|
| Java 17+ | Entidades, serviços e persistência |
| Jakarta Persistence 3.1 | API e anotações padronizadas de ORM |
| Hibernate ORM 6.4.10.Final | Implementação de JPA |
| H2 2.2.224 | Banco relacional em arquivo, acessado por JDBC |
| Maven | Dependências, compilação, testes e empacotamento |
| JUnit Jupiter 5.10.2 | Testes de integração |
| Jupyter e IJava 1.3.0 | Execução interativa de Java |
| Python, nbclient e nbformat | Execução automatizada do notebook |
| PlantUML | Fontes e imagens dos diagramas |

As versões Java foram fixadas no `pom.xml` para reprodução da atividade; não representam uma indicação das versões mais recentes para produção.

## Organização e funcionamento

```text
src/main/java/br/edu/biblioteca/
  Leitor.java, Carteirinha.java, Livro.java, Autor.java
  LivroAutor.java, Exemplar.java, Emprestimo.java
  Database.java              # Inicialização, EntityManagerFactory e transações
  Repository.java            # CRUD e consultas com Criteria API
  BibliotecaService.java     # Empréstimo e devolução
src/main/resources/
  META-INF/persistence.xml   # Unidade de persistência biblioteca
  schema.sql                 # Esquema e restrições específicas do H2
src/test/java/br/edu/biblioteca/PersistenciaTest.java
notebooks/testes_persistencia.ipynb
diagramas/                   # Fontes .puml e imagens .png
scripts/executar_notebook.py
pom.xml
```

`Database` recebe o caminho base do banco e o converte em URL `jdbc:h2:file:...`. O H2 acrescenta `.mv.db` ao arquivo. Primeiro, o esquema SQL cria as estruturas que ainda não existem; em seguida, o Hibernate inicia com `hibernate.hbm2ddl.auto=validate`, verificando a compatibilidade das entidades com as tabelas sem apagar dados.

O esquema explícito mantém as restrições no banco. Para substituir o índice parcial usado na versão SQLite, o H2 calcula `exemplar_ativo_id`: recebe `exemplar_id` enquanto não há devolução e `NULL` após devolver. Um índice único nessa coluna permite vários empréstimos encerrados e impede dois ativos do mesmo exemplar. Essa coluna técnica aparece no E-R, mas não é um atributo editável da entidade Java.

`Database.transaction()` abre um `EntityManager`, inicia a transação, executa a operação e confirma com `commit`. Em caso de erro, faz `rollback` e fecha o contexto. O `Repository` usa `persist`, `find`, `merge`, `remove` e Criteria API. `findBy("livro.id", id)` consulta pelo atributo Java, com valor parametrizado.

Os objetos retornados pelo repositório ficam destacados após a operação. `update()` retorna a cópia resultante de `merge`. A coleção `Livro.exemplares` é carregada sob demanda: deve ser acessada dentro de `Database.transaction()`, ou consultada pelo repositório de `Exemplar`. Para associar um exemplar a um livro, use `exemplar.setLivro(livro)`, pois esse é o lado que grava a FK.

`BibliotecaService` executa cada empréstimo/devolução em uma única transação. Bloqueios pessimistas coordenam alterações concorrentes; a restrição do banco também protege inserções feitas diretamente pelo repositório. Na devolução, o Hibernate detecta a alteração da entidade gerenciada e a grava no commit (dirty checking).

Este projeto acadêmico usa o pool interno do Hibernate e credenciais locais de demonstração. Não implementa migração de dados de arquivos SQLite para H2, interface gráfica ou API HTTP. Ao abrir um banco da versão JPA, use um novo caminho base, não o arquivo SQLite antigo.

## Compilar e testar

Requisitos: JDK 17+, Maven e, para o notebook, Python/Jupyter com kernel IJava.

Na raiz:

```bash
mvn clean package dependency:copy-dependencies -DincludeScope=runtime
```

O `clean` remove artefatos compilados e dependências antigas de `target/`, evitando misturar as versões ORMLite e JPA ao alternar branches. O comando compila, testa e gera o JAR `target/biblioteca-1.0.0.jar`, além das bibliotecas em `target/dependency/`.

Para executar somente os testes Java:

```bash
mvn test
```

Os testes abrangem relacionamentos, CRUD, reabertura do banco em arquivo, rollback de múltiplas operações, restrições de datas e empréstimos concorrentes.

Validação desta migração: **4 testes JUnit aprovados, sem falhas ou erros**, e **8 células de código do notebook executadas com sucesso**, com saídas salvas. Os dois diagramas PNG foram regenerados dos fontes PlantUML e conferidos visualmente.

## Notebook Jupyter

Prepare o ambiente, caso ainda não exista:

```bash
python3 -m venv .venv
.venv/bin/pip install jupyterlab nbclient nbformat
```

Baixe e extraia o [IJava 1.3.0](https://github.com/SpencerPark/IJava/releases/tag/v1.3.0). Na pasta extraída, execute:

```text
/caminho/do/projeto/.venv/bin/python install.py --sys-prefix
```

Na raiz do projeto:

```bash
.venv/bin/jupyter kernelspec list
cd notebooks
../.venv/bin/jupyter lab
```

Abra `testes_persistencia.ipynb`, selecione **Java** e execute tudo. O diretório de execução deve ser `notebooks/`, pois o classpath usa `../target/`. Reinicie o kernel antes de repetir o notebook ou trocar de branch, para não reutilizar classes da versão anterior.

Cada execução cria uma pasta temporária e um novo banco H2, preservando bancos existentes. A etapa final fecha e reabre a fábrica JPA, comprovando a persistência em arquivo. Exceções esperadas de restrições são capturadas; mensagens SQL de violação podem aparecer como parte desses testes. Uma falha inesperada interrompe a execução.

Para executar e salvar as saídas pelo terminal, na raiz:

```bash
.venv/bin/python scripts/executar_notebook.py
```

## Diagramas e Git

Para regenerar as imagens com PlantUML (Smetana, sem Graphviz separado):

```bash
java -Djava.awt.headless=true -jar /caminho/plantuml.jar -tpng diagramas/classes.puml diagramas/er.puml
```

Versione os fontes, as imagens, o notebook com suas saídas, o README e as configurações. `.gitignore` exclui bancos locais, `target/`, `.m2/`, `.tools/` e `.venv/`. As alterações desta versão devem ser commitadas na `develop`; a `main` mantém a implementação anterior enquanto não houver merge.

## Referências

- Slides da aula: *Aula13 — Mapeamento Objeto Relacional*.
- [Tutorial de modelagem da disciplina](https://github.com/marceloakira/tutorials/tree/main/modelagem-orm).
- [Introdução oficial ao Hibernate 6.4 e JPA](https://docs.jboss.org/hibernate/orm/6.4/introduction/html_single/Hibernate_Introduction.html).
- [Configuração JPA com Hibernate](https://docs.hibernate.org/orm/6.4/quickstart/html_single/).
- [Sintaxe SQL e restrições do H2](https://h2database.com/html/grammar.html).
- [Kernel IJava](https://github.com/SpencerPark/IJava).

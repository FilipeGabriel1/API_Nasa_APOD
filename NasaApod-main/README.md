# NasaApod

Aplicação Spring Boot que consulta a API NASA APOD (Astronomy Picture Of the Day), traduz título e explicação para pt-BR e expõe endpoints REST + uma página web estática para visualizar a imagem do dia.

**Integrantes**

- Filipe Gabriel
- Maria Iasmin
- Maria Roberta
- Paulo Cesar

----

## Visão geral

- Linguagem: Java 21
- Framework: Spring Boot 3.x
- Build: Maven (wrapper incluído)
- Documentação automática: OpenAPI / Swagger (springdoc)

O projeto expõe os seguintes endpoints principais:

- `GET /apod` — retorna o objeto APOD em JSON (campos: `title`, `explanation`, `url`).
- `GET /apod/image` — proxy que retorna a mídia (imagem) do APOD como binário (Content-Type apropriado). Se o APOD for vídeo, retorna 204 (não é imagem).
- `GET /` — página estática (index) que busca `/apod` e exibe a imagem/título/explicação no navegador.

Documentação interativa (Swagger UI):

- `http://localhost:8080/swagger-ui.html` (ou `/swagger-ui/index.html`)

----

## Requisitos

- Java 21 (JDK)
- Maven (opcional se usar o `mvnw` wrapper)

## Como executar (Windows PowerShell)

1. Abra o PowerShell e vá para o diretório do projeto:

```powershell
cd 'C:\Users\filip\Downloads\NasaApod-main\NasaApod-main'
```

2. (Opcional) buildar o projeto e gerar o jar:

```powershell
.\mvnw.cmd clean package
```

3. Executar a aplicação:

```powershell
.\mvnw.cmd spring-boot:run
# ou, após package:
java -jar .\target\NasaApod-0.0.1-SNAPSHOT.jar
```

4. Abrir no navegador:

- Página da imagem: `http://localhost:8080/`
- API JSON: `http://localhost:8080/apod`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Configurar a chave da NASA

Edite `src/main/resources/application.properties` e defina a sua chave:

```
nasa.api.url=https://api.nasa.gov/planetary/apod
nasa.api.key=SEU_API_KEY_AQUI
```

Ou execute com argumento do Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--nasa.api.key=SEU_API_KEY_AQUI"
```

## Testes rápidos

- Curl para obter JSON:

```powershell
curl.exe http://localhost:8080/apod
```

- Baixar mídia (binário):

```powershell
curl.exe -o apod_media.bin http://localhost:8080/apod/image
```

## Observações & melhorias

- O endpoint `/apod/image` faz proxy dos bytes da URL retornada pela NASA — isso facilita o download mas usa largura de banda do servidor. Alternativamente, o frontend (`/`) carrega a URL diretamente quando apropriado (especialmente para vídeos).
- Se desejar, podemos adicionar cabeçalhos de cache, respostas de erro padronizadas em JSON ou alterar `/apod/image` para redirecionar (302) para a URL da NASA.

----

Se quiser que eu adicione instruções de deploy (Docker, Heroku, Azure) ou implemente alguma das melhorias acima, diga qual prefere e eu faço as alterações.

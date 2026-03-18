package com.nasa.apod.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.nasa.apod.model.Apod;
import com.nasa.apod.model.BuscaAstronomicaItem;
import com.nasa.apod.service.TraducaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/apod")
public class NasaController {

    // Logger para registrar mensagens de log (info, warn, error)
    private static final Logger LOGGER = LoggerFactory.getLogger(NasaController.class);

    // RestTemplate é usado para fazer chamadas HTTP para APIs externas (NASA, serviço de tradução)
    private final RestTemplate restTemplate;

    // URL montada da API APOD da NASA (baseUrl + ?api_key=APIKEY)
    private final String apodUrl;

    // URL base da API de busca do acervo multimídia da NASA
    private final String nasaSearchUrl;

    // Serviço responsável por traduzir textos para português
    private final TraducaoService traducaoService;

    /**
     * Construtor do controller. Dependências são injetadas pelo Spring.
     * @param restTemplate bean para chamadas HTTP
     * @param baseUrl url base da API da NASA (injetada de application.properties)
     * @param apiKey chave da API da NASA (injetada de application.properties)
     * @param traducaoService serviço que traduz textos (inglês -> pt-BR)
     */
    public NasaController(RestTemplate restTemplate,
                          @Value("${nasa.api.url}") String baseUrl,
                          @Value("${nasa.api.key}") String apiKey,
                          @Value("${nasa.images.api.url}") String nasaSearchUrl,
                          TraducaoService traducaoService) {
        this.restTemplate = restTemplate;
        // Monta a URL completa com a chave de API
        this.apodUrl = String.format("%s?api_key=%s", baseUrl, apiKey);
        this.nasaSearchUrl = nasaSearchUrl;
        this.traducaoService = traducaoService;
    }

    @GetMapping
    @Operation(summary = "Busca APOD (Astronomy Picture of the Day) da NASA")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "APOD retornado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Apod.class))),
        @ApiResponse(responseCode = "502", description = "Resposta vazia da API da NASA"),
        @ApiResponse(responseCode = "503", description = "Serviço indisponível")
    })
    public ResponseEntity<?> buscarApod() {
        try {
            // Faz a chamada GET para a API da NASA e desserializa o JSON em Apod.class
            Apod resposta = restTemplate.getForObject(apodUrl, Apod.class);
            if (resposta == null) {
                // Se a API retornar vazio (null), logamos e retornamos 502 ao cliente
                LOGGER.warn("Resposta vazia da API APOD");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Não foi possível obter os dados da NASA neste momento.");
            }
            
            // Se existir título, traduz para português
            if (resposta.getTitle() != null) {
                resposta.setTitle(traducaoService.traduzirParaPortugues(resposta.getTitle()));
            }
            // Se existir explicação, traduz para português
            if (resposta.getExplanation() != null) {
                resposta.setExplanation(traducaoService.traduzirParaPortugues(resposta.getExplanation()));
            }
            
            // Retorna 200 OK com o objeto Apod (serializado para JSON automaticamente)
            return ResponseEntity.ok(resposta);
        } catch (RestClientResponseException e) {
            // Exceção quando a API externa respondeu com um status HTTP (ex.: 401, 429, 500)
            int statusCode = e.getStatusCode() != null ? e.getStatusCode().value() : 500;
            // Log detalhado com corpo da resposta para depuração
            LOGGER.error("Erro ao chamar a API APOD: status={}, corpo={}", statusCode, e.getResponseBodyAsString(), e);
            // Repasse do status recebido da API externa para o cliente
            return ResponseEntity.status(statusCode)
                    .body("Erro ao acessar a API da NASA: " + e.getStatusText());
        } catch (RestClientException e) {
            // Erros de cliente HTTP (ex.: timeout, problemas de rede)
            LOGGER.error("Erro inesperado ao chamar a API APOD", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Serviço indisponível no momento. Tente novamente mais tarde.");
        }
    }

    @GetMapping("/image")
    @Operation(summary = "Retorna a mídia (imagem) do APOD como binário")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagem retornada com sucesso",
                content = @Content(mediaType = "image/*", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "204", description = "APOD atual é um vídeo ou não há imagem para retornar"),
        @ApiResponse(responseCode = "502", description = "Resposta vazia da API da NASA"),
        @ApiResponse(responseCode = "503", description = "Serviço indisponível")
    })
    public ResponseEntity<?> buscarApodImage() {
        try {
            Apod resposta = restTemplate.getForObject(apodUrl, Apod.class);
            if (resposta == null || resposta.getUrl() == null) {
                LOGGER.warn("Resposta vazia da API APOD ou sem URL de mídia");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Não foi possível obter a URL da mídia da NASA neste momento.");
            }

            String mediaUrl = resposta.getUrl();

            // Busca o conteúdo bruto da mídia (imagem/video) e obtém headers
            ResponseEntity<byte[]> mediaResp = restTemplate.exchange(mediaUrl, HttpMethod.GET, null, byte[].class);
            if (!mediaResp.getStatusCode().is2xxSuccessful() || mediaResp.getBody() == null) {
                LOGGER.warn("Falha ao baixar mídia: status={}", mediaResp.getStatusCode());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Não foi possível baixar a mídia da URL fornecida pela NASA.");
            }

            MediaType contentType = mediaResp.getHeaders().getContentType();
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // Se for vídeo ou HTML, não retornamos como imagem binária (Swagger mostrará download)
            if (contentType.getType().equalsIgnoreCase("video") || MediaType.TEXT_HTML.includes(contentType)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("APOD atual não é uma imagem.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            // Opcional: instruir browser a baixar com nome
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=apod-media");

            return new ResponseEntity<>(mediaResp.getBody(), headers, HttpStatus.OK);
        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode() != null ? e.getStatusCode().value() : 500;
            LOGGER.error("Erro ao chamar a API APOD para mídia: status={}, corpo={}", statusCode, e.getResponseBodyAsString(), e);
            return ResponseEntity.status(statusCode)
                    .body("Erro ao acessar a API da NASA: " + e.getStatusText());
        } catch (RestClientException e) {
            LOGGER.error("Erro inesperado ao obter mídia APOD", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Serviço indisponível no momento. Tente novamente mais tarde.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Busca planetas, estrelas e outros conteúdos no acervo da NASA")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultados de busca retornados com sucesso",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = BuscaAstronomicaItem.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
        @ApiResponse(responseCode = "503", description = "Serviço indisponível")
    })
    public ResponseEntity<?> buscarConteudoAstronomico(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "8") Integer limit) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Informe o parâmetro query para pesquisar.");
        }

        int limiteSeguro = Math.max(1, Math.min(limit, 20));

        try {
            JsonNode resposta = restTemplate.getForObject(
                    nasaSearchUrl + "?q={query}",
                    JsonNode.class,
                    query.trim());

            if (resposta == null || resposta.path("collection").isMissingNode()) {
                LOGGER.warn("Resposta vazia da API de busca da NASA para query={}", query);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Não foi possível obter resultados da NASA neste momento.");
            }

            JsonNode items = resposta.path("collection").path("items");
            List<BuscaAstronomicaItem> resultados = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    if (resultados.size() >= limiteSeguro) {
                        break;
                    }

                    JsonNode data = item.path("data");
                    if (!data.isArray() || data.isEmpty()) {
                        continue;
                    }

                    JsonNode info = data.get(0);
                    String title = textoOuNulo(info, "title");
                    String description = textoOuNulo(info, "description");
                    String mediaType = textoOuNulo(info, "media_type");
                    String nasaId = textoOuNulo(info, "nasa_id");
                    String dateCreated = textoOuNulo(info, "date_created");

                    JsonNode links = item.path("links");
                    String mediaUrl = null;
                    if (links.isArray() && !links.isEmpty()) {
                        mediaUrl = textoOuNulo(links.get(0), "href");
                    }

                    if (title != null) {
                        title = traducaoService.traduzirParaPortugues(title);
                    }

                    if (description != null) {
                        description = traducaoService.traduzirParaPortugues(description);
                    }

                    resultados.add(new BuscaAstronomicaItem(
                            title,
                            description,
                            mediaType,
                            mediaUrl,
                            nasaId,
                            dateCreated));
                }
            }

            return ResponseEntity.ok(resultados);
        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode() != null ? e.getStatusCode().value() : 500;
            LOGGER.error("Erro ao chamar a busca da NASA: status={}, corpo={}", statusCode, e.getResponseBodyAsString(), e);
            return ResponseEntity.status(statusCode)
                    .body("Erro ao acessar a busca da NASA: " + e.getStatusText());
        } catch (RestClientException e) {
            LOGGER.error("Erro inesperado ao buscar conteúdos astronômicos", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Serviço indisponível no momento. Tente novamente mais tarde.");
        }
    }

    private String textoOuNulo(JsonNode node, String campo) {
        JsonNode valor = node.path(campo);
        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }
        String texto = valor.asText();
        return texto == null || texto.isBlank() ? null : texto;
    }
}


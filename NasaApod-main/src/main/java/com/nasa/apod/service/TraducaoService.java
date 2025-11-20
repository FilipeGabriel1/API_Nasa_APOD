package com.nasa.apod.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class TraducaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraducaoService.class);
    private static final String TRADUCAO_API_URL = "https://api.mymemory.translated.net/get?q={texto}&langpair=en|pt-BR";

    private final RestTemplate restTemplate;

    public TraducaoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Tradução pública: tenta traduzir e, em caso de erro, retorna o original
    public String traduzirParaPortugues(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return texto;
        }

        try {
            // MyMemory costuma limitar o tamanho; chama direto se pequeno
            if (texto.length() <= 500) {
                return traduzirTexto(texto);
            }

            StringBuilder resultado = new StringBuilder();
            int inicio = 0;
            final int tamanhoMaximo = 500;

            while (inicio < texto.length()) {
                int fim = Math.min(inicio + tamanhoMaximo, texto.length());
                String parte = texto.substring(inicio, fim);

                // Tenta evitar cortar no meio de uma frase: procura um ponto próximo ao fim
                if (fim < texto.length() && !parte.endsWith(".") && !parte.endsWith("!") && !parte.endsWith("?")) {
                    int ultimoPonto = parte.lastIndexOf('.');
                    if (ultimoPonto > (int) (tamanhoMaximo * 0.7)) {
                        parte = texto.substring(inicio, inicio + ultimoPonto + 1);
                        fim = inicio + ultimoPonto + 1;
                    }
                }

                String parteTraduzida = traduzirTexto(parte);
                resultado.append(parteTraduzida);

                inicio = fim;
                if (inicio < texto.length() && !parteTraduzida.endsWith(" ")) {
                    resultado.append(" ");
                }
            }

            return resultado.toString();
        } catch (Exception e) {
            LOGGER.error("Erro ao traduzir texto, retornando original", e);
            return texto;
        }
    }

    // Chamada à API MyMemory para textos curtos
    private String traduzirTexto(String texto) {
        try {
            RespostaTraducao resposta = restTemplate.getForObject(TRADUCAO_API_URL, RespostaTraducao.class, texto);
            if (resposta != null && resposta.getResponseData() != null && resposta.getResponseData().getTranslatedText() != null) {
                return resposta.getResponseData().getTranslatedText();
            }
            LOGGER.warn("Resposta de tradução inválida, retornando texto original");
            return texto;
        } catch (Exception e) {
            LOGGER.error("Erro ao chamar API de tradução", e);
            return texto;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RespostaTraducao {
        @JsonProperty("responseData")
        private DadosTraducao responseData;

        public DadosTraducao getResponseData() {
            return responseData;
        }

        public void setResponseData(DadosTraducao responseData) {
            this.responseData = responseData;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DadosTraducao {
        @JsonProperty("translatedText")
        private String translatedText;

        public String getTranslatedText() {
            return translatedText;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }
    }
}


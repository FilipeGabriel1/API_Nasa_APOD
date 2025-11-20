package com.nasa.apod.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Apod {

    // Título da imagem do dia (vindo da API da NASA)
    private String title;

    // Descrição/explicação da imagem (texto que pode ser longo)
    private String explanation;

    // URL da imagem ou do vídeo (link externo fornecido pela NASA)
    private String url;

    public Apod() {
    }

    public Apod(String title, String explanation, String url) {
        this.title = title;
        this.explanation = explanation;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}


package com.nasa.apod.model;

public class BuscaAstronomicaItem {

    private String title;
    private String description;
    private String mediaType;
    private String mediaUrl;
    private String nasaId;
    private String dateCreated;

    public BuscaAstronomicaItem() {
    }

    public BuscaAstronomicaItem(String title,
                                String description,
                                String mediaType,
                                String mediaUrl,
                                String nasaId,
                                String dateCreated) {
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.nasaId = nasaId;
        this.dateCreated = dateCreated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getNasaId() {
        return nasaId;
    }

    public void setNasaId(String nasaId) {
        this.nasaId = nasaId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}

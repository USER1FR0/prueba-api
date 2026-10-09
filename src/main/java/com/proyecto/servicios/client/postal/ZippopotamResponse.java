package com.proyecto.servicios.client.postal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Respuesta de zippopotam.us: GET /{country}/{postalcode}
 * 404 si no existe. "places" vacio tambien se trata como no existente.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZippopotamResponse {

    private String country;
    private List<Place> places;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public List<Place> getPlaces() { return places; }
    public void setPlaces(List<Place> places) { this.places = places; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Place {
        private String placeName;
        private String state;

        public String getPlaceName() { return placeName; }
        public void setPlaceName(String placeName) { this.placeName = placeName; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }
}

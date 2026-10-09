package com.proyecto.servicios.client.postal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Respuesta de zipcodestack: GET /v1/search?codes={cp}&country={iso}
 * El nodo "results" es un mapa cp -> lista de localidades. Lista vacia = no existe.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipcodestackResponse {

    private Map<String, List<PostalLocation>> results;

    public Map<String, List<PostalLocation>> getResults() {
        return results;
    }

    public void setResults(Map<String, List<PostalLocation>> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostalLocation {
        private String postalCode;
        private String city;
        private String state;
        private String countryCode;

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }
}

package ru.maxmorev.eshop.customer.api.rest.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMappedValue {
    private static ObjectMapper MAPPER = new ObjectMapper();

    public String toJsonString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

}

package com.chargev.emsp.entity.poi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;


@Converter
public class ConnectorStrConverter implements AttributeConverter<ArrayList<ConnectorStr>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ArrayList<ConnectorStr> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert ArrayList<ConnectorStr> to JSON string.", e);
        }
    }

    @Override
    public ArrayList<ConnectorStr> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, ConnectorStr.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
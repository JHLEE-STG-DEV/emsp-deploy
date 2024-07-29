package com.chargev.emsp.entity.poi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;


@Converter
public class EvseConverter implements AttributeConverter<ArrayList<EvseStr>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ArrayList<EvseStr> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert ArrayList<EvseStr> to JSON string.", e);
        }
    }

    @Override
    public ArrayList<EvseStr> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, EvseStr.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
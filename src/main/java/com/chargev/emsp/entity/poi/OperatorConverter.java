package com.chargev.emsp.entity.poi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;

@Converter
public class OperatorConverter implements AttributeConverter<Operator, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Operator attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert Operator to JSON string.", e);
        }
    }

    @Override
    public Operator convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Operator.class);
        } catch (Exception e) {
            return new Operator();  
        }
    }
}

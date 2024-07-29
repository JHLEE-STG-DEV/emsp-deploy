package com.chargev.utils;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonHelper{
    
    private final ObjectMapper mapper;

    public <T> String objectToString(T objectData) {

        String asString = "";
        try {
            asString = mapper.writeValueAsString(objectData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return asString;
    }
    public <T> T stringToObject(String jsonString, Class<T> classType) {
        T object = null;
        try {
            object = mapper.readValue(jsonString, classType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return object;
    }
}

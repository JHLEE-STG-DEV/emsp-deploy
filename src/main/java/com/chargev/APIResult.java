package com.chargev;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class APIResult<T> {

    @JsonProperty("resultCode")
    private int resultCode;

    @JsonProperty("resultValue")
    private String resultValue;

    @JsonProperty("data")
    private T data;
}

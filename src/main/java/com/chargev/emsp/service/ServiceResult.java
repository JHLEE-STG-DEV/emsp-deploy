package com.chargev.emsp.service;

import java.util.Optional;

import lombok.Data;

@Data
public class ServiceResult<T> {
    private boolean success;
    private int errorCode = 500;
    private String errorMessage = "Unknwon Error";
    private String refId;
    private Optional<T> data = Optional.empty();

    public boolean getSuccess() {
        return this.success && data.isPresent();
    }

    public T get() {
        if (data.isEmpty())
            return null;
        return data.get();
    }

    public void succeed(T data) {
        if (data == null)
            return;

        this.errorCode = 200;
        this.success = true;
        this.data = Optional.of(data);
    }

    public void fail(int errorCode, String message) {
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.success = false;
        this.data = Optional.empty();
    }

}
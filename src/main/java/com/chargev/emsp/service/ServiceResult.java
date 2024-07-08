package com.chargev.emsp.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.chargev.emsp.service.log.CheckpointReference;

import lombok.Data;

@Data
public class ServiceResult<T> {
    private boolean success;
    private int errorCode = 500;
    private String errorMessage = "Unknwon Error";
    //private String refId;
    private Optional<T> data = Optional.empty();

    public boolean getSuccess() {
        return this.success && (data.isPresent());
    }
    public boolean isFail(){
        return !getSuccess();
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
        this.data = Optional.ofNullable(data);
    }

    public void fail(int errorCode, String message) {
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.success = false;
        this.data = Optional.empty();
    }

    // 로그용
    private List<CheckpointReference> checkpoints = new LinkedList<>();
}
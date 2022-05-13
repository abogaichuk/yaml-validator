package com.example.yamlvalidator.services;

public interface MessageProvider {
    String getMessage(String code);
    String getMessage(String code, Object... arguments);
}

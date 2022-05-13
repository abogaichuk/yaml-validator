package com.example.yamlvalidator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ResourceMessageProvider implements MessageProvider {
    @Autowired
    private MessageSource messageSource;

    @Override
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    @Override
    public String getMessage(String code, Object... arguments) {
        return messageSource.getMessage(code, arguments, Locale.ENGLISH);
    }
}

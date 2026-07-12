package com.delivery.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class CryptoConverter implements AttributeConverter<String, String> {
    private final SsnEncryptor ssnEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return ssnEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return ssnEncryptor.decrypt(dbData);
    }
}

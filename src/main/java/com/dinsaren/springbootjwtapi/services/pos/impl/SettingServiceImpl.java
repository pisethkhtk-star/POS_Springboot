package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.payload.pos.SettingsDTO;
import com.dinsaren.springbootjwtapi.services.pos.SettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class SettingServiceImpl implements SettingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File settingsFile = new File("store-settings.json");

    @Override
    public SettingsDTO getSettings() {
        if (settingsFile.exists()) {
            try {
                return objectMapper.readValue(settingsFile, SettingsDTO.class);
            } catch (IOException ignored) {}
        }
        return new SettingsDTO();
    }

    @Override
    public SettingsDTO updateSettings(SettingsDTO request) {
        SettingsDTO current = getSettings();

        if (request.getStoreName() != null) current.setStoreName(request.getStoreName());
        if (request.getStoreAddress() != null) current.setStoreAddress(request.getStoreAddress());
        if (request.getStorePhone() != null) current.setStorePhone(request.getStorePhone());
        if (request.getTaxRate() != null) current.setTaxRate(request.getTaxRate());
        if (request.getReceiptHeader() != null) current.setReceiptHeader(request.getReceiptHeader());
        if (request.getReceiptFooter() != null) current.setReceiptFooter(request.getReceiptFooter());

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, current);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings: " + e.getMessage());
        }

        return current;
    }
}

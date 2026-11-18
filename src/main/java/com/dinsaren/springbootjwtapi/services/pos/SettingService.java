package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.payload.pos.SettingsDTO;

public interface SettingService {
    SettingsDTO getSettings();
    SettingsDTO updateSettings(SettingsDTO request);
}

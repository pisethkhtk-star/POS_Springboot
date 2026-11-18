package com.dinsaren.springbootjwtapi.services.pos;

import java.util.Map;

public interface PosTelegramService {
    Map<String, Boolean> sendReceipt(Map<String, Object> body);
}

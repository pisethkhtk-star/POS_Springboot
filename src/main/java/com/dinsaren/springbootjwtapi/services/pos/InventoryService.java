package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.payload.pos.InventoryAdjustRequest;

import java.util.List;

public interface InventoryService {
    List<InventoryLog> getAllLogs();
    Object adjustStock(InventoryAdjustRequest request, Integer currentUserId, String currentUserName);
}

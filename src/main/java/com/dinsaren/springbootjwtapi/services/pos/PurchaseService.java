package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Purchase;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseReceiveRequest;

import java.util.List;

public interface PurchaseService {
    List<Purchase> getAllPurchases();
    List<Purchase> getPurchasesByStatus(String status);
    Purchase getPurchaseById(Integer id);
    Purchase createPurchase(PurchaseCreateRequest request, Integer currentUserId);
    Purchase receivePurchase(Integer purchaseId, PurchaseReceiveRequest receiveRequest, Integer currentUserId);
    Purchase cancelPurchase(Integer purchaseId, Integer currentUserId);
}

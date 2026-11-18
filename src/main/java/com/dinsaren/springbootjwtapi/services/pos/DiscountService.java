package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Discount;
import com.dinsaren.springbootjwtapi.payload.pos.DiscountRequest;

import java.util.List;

public interface DiscountService {
    List<Discount> getAllDiscounts();
    Discount getDiscountById(Integer id);
    Discount createDiscount(DiscountRequest request);
    Discount updateDiscount(Integer id, DiscountRequest request);
    void deleteDiscount(Integer id);
}

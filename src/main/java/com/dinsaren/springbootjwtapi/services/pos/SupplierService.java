package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Supplier;
import com.dinsaren.springbootjwtapi.payload.pos.SupplierRequest;

import java.util.List;

public interface SupplierService {
    List<Supplier> searchSuppliers(String search);
    Supplier getSupplierById(Integer id);
    Supplier createSupplier(SupplierRequest request);
    Supplier updateSupplier(Integer id, SupplierRequest request);
    void deleteSupplier(Integer id);
}

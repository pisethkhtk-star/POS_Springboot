package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Supplier;
import com.dinsaren.springbootjwtapi.payload.pos.SupplierRequest;
import com.dinsaren.springbootjwtapi.repository.pos.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> searchSuppliers(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return supplierRepository.searchSuppliers(search.trim());
        }
        return supplierRepository.findAllByOrderByCreatedAtDesc();
    }

    public Supplier getSupplierById(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    @Transactional
    public Supplier createSupplier(SupplierRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier name is required");
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName().trim());
        supplier.setContact(request.getContact());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Integer id, SupplierRequest request) {
        Supplier supplier = getSupplierById(id);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            supplier.setName(request.getName().trim());
        }
        if (request.getContact() != null) {
            supplier.setContact(request.getContact());
        }
        if (request.getPhone() != null) {
            supplier.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            supplier.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Integer id) {
        Supplier supplier = getSupplierById(id);
        supplierRepository.delete(supplier);
    }
}

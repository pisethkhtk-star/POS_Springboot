package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.PosCustomer;
import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.payload.pos.CustomerRequest;
import com.dinsaren.springbootjwtapi.repository.pos.PosCustomerRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosOrderRepository;
import com.dinsaren.springbootjwtapi.services.pos.PosCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PosCustomerServiceImpl implements PosCustomerService {

    private final PosCustomerRepository posCustomerRepository;
    private final PosOrderRepository posOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PosCustomer> searchCustomers(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return posCustomerRepository.searchCustomers(search.trim());
        }
        return posCustomerRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public PosCustomer getCustomerById(Integer id) {
        return posCustomerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @Override
    @Transactional
    public PosCustomer createCustomer(CustomerRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer name is required");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String phone = request.getPhone().trim();
            if (posCustomerRepository.existsByPhone(phone)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
            }
        }

        PosCustomer customer = new PosCustomer();
        customer.setName(request.getName().trim());
        customer.setPhone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : null);
        customer.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : null);
        customer.setAddress(request.getAddress());
        customer.setPoints(request.getPoints() != null ? request.getPoints() : 0);

        return posCustomerRepository.save(customer);
    }

    @Override
    @Transactional
    public PosCustomer updateCustomer(Integer id, CustomerRequest request) {
        PosCustomer customer = getCustomerById(id);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            customer.setName(request.getName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(customer.getPhone()) && posCustomerRepository.existsByPhone(newPhone)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already registered by another customer");
            }
            customer.setPhone(newPhone);
        }

        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getPoints() != null) {
            customer.setPoints(request.getPoints());
        }

        return posCustomerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Integer id) {
        PosCustomer customer = getCustomerById(id);

        // Nullify customer reference in existing orders to preserve transaction history
        List<PosOrder> orders = posOrderRepository.findAll();
        for (PosOrder order : orders) {
            if (order.getCustomerId() != null && order.getCustomerId().equals(id)) {
                order.setCustomerId(null);
                posOrderRepository.save(order);
            }
        }

        posCustomerRepository.delete(customer);
    }
}

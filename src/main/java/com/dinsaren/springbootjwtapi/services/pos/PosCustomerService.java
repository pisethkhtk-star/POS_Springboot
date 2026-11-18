package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosCustomer;
import com.dinsaren.springbootjwtapi.payload.pos.CustomerRequest;

import java.util.List;

public interface PosCustomerService {
    List<PosCustomer> searchCustomers(String search);
    PosCustomer getCustomerById(Integer id);
    PosCustomer createCustomer(CustomerRequest request);
    PosCustomer updateCustomer(Integer id, CustomerRequest request);
    void deleteCustomer(Integer id);
}

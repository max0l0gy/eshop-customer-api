package ru.maxmorev.eshop.customer.api.service;


import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;

import java.util.Optional;

public interface CustomerService {

    Customer createCustomerAndVerifyByEmail(CustomerDto customer);

    Customer createAdminAndVerifyByEmail(CustomerDto customer);

    Customer updateInfo(CustomerDto i);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> verify(Long customerId, String code);

    Optional<Customer> generateResetPasswordCode(Long customerId);

    Optional<Customer> updatePassword(Long customerId, String resetPasswordCode, String password);

}

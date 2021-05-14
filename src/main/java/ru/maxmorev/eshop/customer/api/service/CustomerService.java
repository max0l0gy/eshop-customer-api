package ru.maxmorev.eshop.customer.api.service;


import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;

import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    Customer createCustomerAndVerifyByEmail(CustomerDto customer);

    Customer createAdminAndVerifyByEmail(CustomerDto customer);

    Customer updateInfo(CustomerDto i);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> verify(Long customerId, String code);

    Optional<Customer> generateResetPasswordCode(String email);

    Optional<Customer> updatePassword(Long customerId, UUID resetPasswordCode, String newPassword);

    Customer encodePassword(Customer customer, String newPassword);

    boolean isPasswordMatches(CharSequence rawPassword, String encodedPassword);

}

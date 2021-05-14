package ru.maxmorev.eshop.customer.api.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.maxmorev.eshop.customer.api.entities.Customer;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdAndResetPasswordCode(Long id, UUID resetPasswordCode);
}

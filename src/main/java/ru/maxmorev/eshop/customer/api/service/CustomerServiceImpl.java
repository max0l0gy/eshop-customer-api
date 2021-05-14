package ru.maxmorev.eshop.customer.api.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maxmorev.eshop.customer.api.annotation.AuthorityValues;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.repository.CustomerRepository;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional
@Service("customerService")
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;
    private PasswordEncoder bcryptEncoder;
    private MessageSource messageSource;

    protected void checkEmail(CustomerDto customer) {
        findByEmail(customer.getEmail())
                .ifPresent(c -> new IllegalArgumentException(
                        messageSource.getMessage("customer.error.unique.email",
                                new Object[]{c.getEmail()},
                                LocaleContextHolder.getLocale())
                ));
    }

    @Override
    @Transactional
    public Customer createCustomerAndVerifyByEmail(CustomerDto customerToCreate) {
        Customer created = null;
        checkEmail(customerToCreate);
        Customer fromDto = CustomerDto.from(customerToCreate);
        fromDto.setVerifyCode(RandomStringUtils.randomAlphabetic(5));
        this.encodePassword(fromDto, customerToCreate.getPassword());
        fromDto.removeAllAuthorities();
        fromDto.addAuthority(AuthorityValues.CUSTOMER);
        created = customerRepository.save(fromDto);
        return created;
    }

    @Override
    @Transactional
    public Customer createAdminAndVerifyByEmail(CustomerDto customerToCreate) {
        Customer created = null;
        checkEmail(customerToCreate);
        Customer fromDto = CustomerDto.from(customerToCreate);
        fromDto.setVerifyCode(RandomStringUtils.randomAlphabetic(5));
        this.encodePassword(fromDto, customerToCreate.getPassword());
        fromDto.addAuthority(AuthorityValues.ADMIN);
        created = customerRepository.save(fromDto);
        return created;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Optional<Customer> verify(Long customerId, String code) {
        Optional<Customer> c = customerRepository.findById(customerId);
        c.ifPresent(customer -> {
            if (code.equals(customer.getVerifyCode())) {
                customer.setVerified(true);
                customerRepository.save(customer);
            }
        });
        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound",
                        new Object[]{username}, LocaleContextHolder.getLocale())));
    }

    @Override
    public Customer updateInfo(CustomerDto customerInfo) {
        Customer findByEmail = findByEmail(customerInfo.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.error.notFound",
                                new Object[]{customerInfo.getEmail()}, LocaleContextHolder.getLocale()))
                );
        findByEmail.setAddress(customerInfo.getAddress());
        findByEmail.setCity(customerInfo.getCity());
        findByEmail.setPostcode(customerInfo.getPostcode());
        findByEmail.setCountry(customerInfo.getCountry());
        findByEmail.setFullName(customerInfo.getFullName());
        return customerRepository.save(findByEmail);
    }

    @Override
    @Transactional
    public Optional<Customer> generateResetPasswordCode(String email) {
        return customerRepository.findByEmail(email)
                .map(customer -> {
                    customer.setResetPasswordCode(UUID.randomUUID());
                    return customerRepository.save(customer);
                });
    }

    @Override
    @Transactional
    public Optional<Customer> updatePassword(Long customerId, UUID resetPasswordCode, String newPassword) {
        return customerRepository.findByIdAndResetPasswordCode(customerId, resetPasswordCode)
                .map(customer -> {
                    customer.setResetPasswordCode(null);
                    this.encodePassword(customer, newPassword);
                    return customerRepository.save(customer);
                });
    }

    @Override
    public Customer encodePassword(Customer customer, String newPassword) {
        return customer.setPassword(bcryptEncoder.encode(newPassword));
    }

    @Override
    public boolean isPasswordMatches(CharSequence rawPassword, String encodedPassword) {
        return bcryptEncoder.matches(rawPassword, encodedPassword);
    }
}

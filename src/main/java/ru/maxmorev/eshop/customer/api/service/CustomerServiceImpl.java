package ru.maxmorev.eshop.customer.api.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maxmorev.eshop.customer.api.annotation.AuthorityValues;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.entities.CustomerInfo;
import ru.maxmorev.eshop.customer.api.repository.CustomerRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Slf4j
@Transactional
@Service("customerService")
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;
    private PasswordEncoder bcryptEncoder;
    private MessageSource messageSource;
    @PersistenceContext
    private EntityManager em;

    protected void checkEmail(Customer customer) {
        findByEmail(customer.getEmail())
                .ifPresent(c -> new IllegalArgumentException(
                        messageSource.getMessage("customer.error.unique.email",
                                new Object[]{c.getEmail()},
                                LocaleContextHolder.getLocale())
                ));
    }

    @Override
    public Customer createCustomerAndVerifyByEmail(Customer customer) {
        Customer created = null;
        checkEmail(customer);
        customer.setVerifyCode(RandomStringUtils.randomAlphabetic(5));
        customer.setPassword(bcryptEncoder.encode(customer.getPassword()));
        customer.removeAllAuthorities();
        customer.addAuthority(AuthorityValues.CUSTOMER);
        created = customerRepository.save(customer);
        em.flush();
        return created;
    }

    @Override
    public Customer createAdminAndVerifyByEmail(Customer customer) {
        Customer created = null;
        checkEmail(customer);
        customer.setVerifyCode(RandomStringUtils.randomAlphabetic(5));
        customer.setPassword(bcryptEncoder.encode(customer.getPassword()));
        customer.addAuthority(AuthorityValues.ADMIN);
        created = customerRepository.save(customer);
        em.flush();
        return created;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public void update(Customer customer) {
        this.customerRepository.save(customer);
    }

    @Override
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

    @Transactional(readOnly = true)
    @Override
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
    public Customer updateInfo(CustomerInfo i) {
        Customer findByEmail = findByEmail(i.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.error.notFound",
                                new Object[]{i.getEmail()}, LocaleContextHolder.getLocale()))
                );

        findByEmail.setAddress(i.getAddress());
        findByEmail.setCity(i.getCity());
        findByEmail.setPostcode(i.getPostcode());
        findByEmail.setCountry(i.getCountry());
        findByEmail.setFullName(i.getFullName());
        return customerRepository.save(findByEmail);
    }
}

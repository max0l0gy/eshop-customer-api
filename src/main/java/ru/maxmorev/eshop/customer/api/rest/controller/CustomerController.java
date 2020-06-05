package ru.maxmorev.eshop.customer.api.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.entities.CustomerInfo;
import ru.maxmorev.eshop.customer.api.rest.request.CustomerVerify;
import ru.maxmorev.eshop.customer.api.service.CustomerService;

import javax.validation.Valid;
import java.util.Locale;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final MessageSource messageSource;

    @RequestMapping(path = "/customer/", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Customer createCustomer(@RequestBody @Valid Customer customer, Locale locale) {
        log.info("Customer : {}", customer);
        return customerService.createCustomerAndVerifyByEmail(customer);
    }

    @RequestMapping(path = "/update/", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Customer updateCustomer(@RequestBody @Valid CustomerInfo customer, Locale locale) {
        log.info("Customer update : {}", customer);
        Customer findByEmail = customerService.updateInfo(customer);
        return findByEmail;
    }

    @RequestMapping(path = "/customer/verify/", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerVerify verifyCustomer(@RequestBody @Valid CustomerVerify customerVerify, Locale locale) {
        log.info("CustomerVerify : {}", customerVerify);
        Customer customer = customerService
                .verify(customerVerify.getId(), customerVerify.getVerifyCode())
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound", new Object[]{customerVerify.getId()}, locale)));
        customerVerify.setVerified(customer.getVerified());
        return customerVerify;
    }

    @RequestMapping(path = "/customer/email/{email}", method = RequestMethod.GET)
    @ResponseBody
    public Customer findByEmail(@PathVariable(name = "email") String email, Locale locale) {
        return customerService
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound.email", new Object[]{email}, locale)));
    }

    @RequestMapping(path = "/customer/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Customer findById(@PathVariable(name = "id") Long id, Locale locale) {
        return customerService
                .findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound.email", new Object[]{id}, locale)));
    }

}

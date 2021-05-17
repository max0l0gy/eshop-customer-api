package ru.maxmorev.eshop.customer.api.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.rest.request.CustomerVerify;
import ru.maxmorev.eshop.customer.api.rest.request.UpdatePasswordRequest;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;
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
    public CustomerDto createCustomer(@RequestBody @Valid CustomerDto customer, Locale locale) {
        log.info("Customer : {}", customer);
        return CustomerDto.of(customerService.createCustomerAndVerifyByEmail(customer));
    }

    @RequestMapping(path = "/admin/", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerDto createAdmin(@RequestBody @Valid CustomerDto customer, Locale locale) {
        log.info("Customer : {}", customer);
        return CustomerDto.of(customerService.createAdminAndVerifyByEmail(customer));
    }

    @RequestMapping(path = "/update/", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerDto updateCustomer(@RequestBody @Valid CustomerDto customer, Locale locale) {
        log.info("Customer update : {}", customer);
        return CustomerDto.of(customerService.updateInfo(customer));
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
    public CustomerDto findByEmail(@PathVariable(name = "email") String email, Locale locale) {
        return customerService
                .findByEmail(email)
                .map(CustomerDto::of)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound.email", new Object[]{email}, locale)));
    }

    @RequestMapping(path = "/customer/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CustomerDto findById(@PathVariable(name = "id") Long id, Locale locale) {
        return customerService
                .findById(id)
                .map(CustomerDto::of)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound.email", new Object[]{id}, locale)));
    }

    @GetMapping(path = "/customer/reset-password-code/email/{email}")
    @ResponseBody
    public CustomerDto generateResetPasswordCode(@PathVariable(name = "email") String email, Locale locale) {
        return customerService
                .generateResetPasswordCode(email)
                .map(CustomerDto::of)
                .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("customer.error.notFound", new Object[]{email}, locale)));
    }

    @PostMapping(path = "/customer/update-password")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerDto updatePassword(@RequestBody
                                      @Valid UpdatePasswordRequest updatePasswordRequest,
                                      Locale locale) {
        return customerService
                .updatePassword(updatePasswordRequest)
                .map(CustomerDto::of)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage("customer.error.notFound",
                                new Object[]{updatePasswordRequest.getCustomerEmail()}, locale)
                ));
    }

}

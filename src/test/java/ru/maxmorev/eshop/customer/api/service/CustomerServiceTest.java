package ru.maxmorev.eshop.customer.api.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.maxmorev.eshop.customer.api.annotation.AuthorityValues;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.entities.CustomerAuthority;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 4555)
@RunWith(SpringRunner.class)
@SpringBootTest
@DisplayName("Integration Customer Service Test")
public class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;
    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("should create customer")
    @Transactional
    @SqlGroup({
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void testCreateCustomerAndVerifyByEmail() {
        stubFor(WireMock.post(urlEqualTo("/send/template/"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mailSend.ok.json")));
        Customer customer = new Customer()
                .setEmail("test@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAddress("Test Address")
                .setAuthorities(AuthorityValues.ADMIN.name())
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");
        customer = customerService.createCustomerAndVerifyByEmail(CustomerDto.of(customer));
        em.flush();
        log.info("Customer#VerifyCode {}", customer.getVerifyCode());
        log.info("Customer#passwdord {}", customer.getPassword());
        assertFalse(customer.getVerifyCode().isEmpty());
        assertTrue(customer.getAuthorities().contains(new CustomerAuthority(AuthorityValues.CUSTOMER)));
        assertFalse(customer.getAuthorities().contains(new CustomerAuthority(AuthorityValues.ADMIN)));
    }

    @Test
    @DisplayName("should throw exception while create customer cause address is null")
    @Transactional
    public void testErrorCreateCustomerAndVerifyByEmail() {
        Customer customer = new Customer()
                .setEmail("test@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAuthorities(AuthorityValues.ADMIN.name())
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");
        assertThrows(javax.validation.ConstraintViolationException.class, () -> {
                    customerService.createCustomerAndVerifyByEmail(CustomerDto.of(customer));
                    em.flush();
                }
        );
    }

    @Test
    @DisplayName("should find customer by email")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void testFindByEmail() {
        Optional<Customer> customer = customerService.findByEmail("test@titsonfire.store");
        assertTrue(customer.isPresent());
    }

    @Test
    @DisplayName("should find customer by id")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void testFindById() {
        // Optional<Customer> find(Long id); 10
        Optional<Customer> customer = customerService.findById(10L);
        assertTrue(customer.isPresent());
        assertFalse(customer.get().getVerified());
    }

    @Test
    @Transactional
    @DisplayName("should verify customer by id and verify code")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void testVerify() {
        Optional<Customer> customer = customerService.verify(10L, "TKYOC");
        em.flush();
        assertTrue(customer.isPresent());
        assertTrue(customer.get().getVerified());
    }

    @Test
    @Transactional
    @DisplayName("should not verify customer by id and verify code")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void testVerifyError() {
        Optional<Customer> customer = customerService.verify(10L, "TKYOX");
        em.flush();
        assertTrue(customer.isPresent());
        assertFalse(customer.get().getVerified());
    }

    @Test
    @Transactional
    @DisplayName("should update customer info")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void updateInfoTest() {
        Optional<Customer> customer = customerService.findByEmail("test@titsonfire.store");
        assertTrue(customer.isPresent());
        CustomerDto c = CustomerDto.of(customer.get());
        c.setCountry("Canada");
        c.setCity("Toronto");
        Customer result = customerService.updateInfo(c);
        em.flush();
        assertEquals("Canada", result.getCountry());
        assertEquals("Toronto", result.getCity());
    }

    @Test
    @Transactional
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void generateResetPasswordCode() {
        Optional<Customer> customerBeforeReset = customerService.findByEmail("test@titsonfire.store");
        assertTrue(customerBeforeReset.isPresent());
        assertNull(customerBeforeReset.get().getResetPasswordCode());
        Optional<Customer> customerWithResetPassword = customerService.generateResetPasswordCode("test@titsonfire.store");
        assertTrue(customerWithResetPassword.isPresent());
        assertNotNull(customerWithResetPassword.get().getResetPasswordCode());
    }

    @Test
    @Transactional
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void updatePassword() {
        Optional<Customer> customerBeforePasswordUpdate = customerService.findById(15L);
        assertTrue(customerBeforePasswordUpdate.isPresent());
        assertNotNull(customerBeforePasswordUpdate.get().getResetPasswordCode());
        assertEquals("$2a$10$um0PcvHczmxeUEbR3vCBGuOvtNdgJffm72knavG/EFE7JDm9QBEha", customerBeforePasswordUpdate.get().getPassword());
        Optional<Customer> customerWithUpdatedPassword = customerService
                .updatePassword(15L,
                        UUID.fromString("f6d56466-b345-11eb-8529-0242ac130003"),
                        "newPassword"
                );
        assertTrue(customerWithUpdatedPassword.isPresent());
        assertNull(customerWithUpdatedPassword.get().getResetPasswordCode());
        assertTrue(
                customerService.isPasswordMatches("newPassword", customerWithUpdatedPassword.get().getPassword())
        );
    }


}

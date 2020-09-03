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
import ru.maxmorev.eshop.customer.api.entities.CustomerInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        Customer customer = Customer
                .builder()
                .email("test@titsonfire.store")
                .fullName("Maxim Morev")
                .address("Test Address")
                .authorities(AuthorityValues.ADMIN.name())
                .postcode("111123")
                .city("Moscow")
                .country("Russia")
                .password("helloFreakBitches")
                .build();
        customer = customerService.createCustomerAndVerifyByEmail(customer);
        em.flush();
        log.info("Customer#VerifyCode {}", customer.getVerifyCode());
        log.info("Customer#passwdord {}", customer.getPassword());
        assertFalse(customer.getVerifyCode().isEmpty());
        assertTrue(customer.getAuthorities().contains(new CustomerAuthority(AuthorityValues.CUSTOMER)));
        assertFalse(customer.getAuthorities().contains(new CustomerAuthority(AuthorityValues.ADMIN)));
    }

    @Test
    @DisplayName("should throw exception while create customer couse address is null")
    @Transactional
    public void testErrorCreateCustomerAndVerifyByEmail() {

        Customer customer = Customer
                .builder()
                .email("test@titsonfire.store")
                .fullName("Maxim Morev")
                .authorities(AuthorityValues.ADMIN.name())
                .postcode("111123")
                .city("Moscow")
                .country("Russia")
                .password("helloFreakBitches")
                .build();
        assertThrows(javax.validation.ConstraintViolationException.class, () -> customerService.createCustomerAndVerifyByEmail(customer));
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
        Customer c = customer.get();
        c.setCountry("Canada");
        c.setCity("Toronto");
        Customer result = customerService.updateInfo((CustomerInfo) c);
        em.flush();
        assertEquals("Canada", result.getCountry());
        assertEquals("Toronto", result.getCity());
    }

    @Test
    @Transactional
    @DisplayName("should update customer: create shopping cart and set it")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void updateCustomerTest() {
        Optional<Customer> customer = customerService.findByEmail("test@titsonfire.store");
        assertTrue(customer.isPresent());

        Customer c = customer.get();
        c.setShoppingCartId(100L);
        customerService.update(c);

        em.flush();

        customer = customerService.findByEmail("test@titsonfire.store");
        assertEquals(100L, customer.get().getShoppingCartId().longValue());

    }


}

package ru.maxmorev.eshop.customer.api.controller;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.maxmorev.eshop.customer.api.annotation.AuthorityValues;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.rest.request.CustomerVerify;
import ru.maxmorev.eshop.customer.api.rest.request.UpdatePasswordRequest;
import ru.maxmorev.eshop.customer.api.rest.response.CustomerDto;
import ru.maxmorev.eshop.customer.api.service.CustomerService;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@DisplayName("Integration controller (CustomerController) test")
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("Should create customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void createCustomerTest() throws Exception {
        Customer customer = new Customer()
                .setEmail("test@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAddress("Test Address")
                .setAuthorities(AuthorityValues.ADMIN.name())
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");
        mockMvc.perform(post("/customer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@titsonfire.store")))
                .andExpect(jsonPath("$.fullName", is("Maxim Morev")))
                .andExpect(jsonPath("$.country", is("Russia")))
                .andExpect(jsonPath("$.postcode", is("111123")))
                .andExpect(jsonPath("$.city", is("Moscow")))
                .andExpect(jsonPath("$.address", is("Test Address")))
                .andExpect(jsonPath("$.password", notNullValue()))
                .andExpect(jsonPath("$.verifyCode", notNullValue()))
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.shoppingCartId", nullValue()))
                .andExpect(jsonPath("$.resetPasswordCode", nullValue()))
                .andExpect(jsonPath("$.authorities[0].authority", is("CUSTOMER")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Should create admin from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void createAdminTest() throws Exception {
        Customer customer = new Customer()
                .setEmail("admin@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAddress("Test Address")
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");

        mockMvc.perform(post("/admin/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@titsonfire.store")))
                .andExpect(jsonPath("$.authorities[0].authority", is("ADMIN")))
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.email", is("admin@titsonfire.store")))
                .andExpect(jsonPath("$.fullName", is("Maxim Morev")))
                .andExpect(jsonPath("$.country", is("Russia")))
                .andExpect(jsonPath("$.postcode", is("111123")))
                .andExpect(jsonPath("$.city", is("Moscow")))
                .andExpect(jsonPath("$.address", is("Test Address")))
                .andExpect(jsonPath("$.password", notNullValue()))
                .andExpect(jsonPath("$.verifyCode", notNullValue()))
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.shoppingCartId", nullValue()))
                .andExpect(jsonPath("$.resetPasswordCode", nullValue()))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Should except error while create customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void createCustomerUniqueErrorTest() throws Exception {
        assertTrue(customerService.findByEmail("test@titsonfire.store").isPresent());
        Customer customer = new Customer()
                .setEmail("test@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAddress("Test Address")
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");

        mockMvc.perform(post("/customer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.message", is("Internal storage error")));
    }

    @Test
    @DisplayName("Should except validation error while create customer from RequestBody")
    @Transactional
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void createCustomerValidationErrorTest() throws Exception {
        assertTrue(customerService.findByEmail("test@titsonfire.store").isPresent());
        Customer customer = new Customer()
                .setEmail("test2@titsonfire.store")
                .setFullName("Maxim Morev")
                .setAddress("")
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");

        mockMvc.perform(post("/customer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Validation error")))
                .andExpect(jsonPath("$.errors[0].field", is("address")))
                .andExpect(jsonPath("$.errors[0].message", is("Address cannot be empty")));
    }

    @Test
    @DisplayName("Should except validation errors while create customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void createCustomerValidationErrorFullListTest() throws Exception {
        Customer customer = new Customer()
                .setEmail("")
                .setFullName("")
                .setAddress("")
                .setPostcode("")
                .setCity("")
                .setCountry("")
                .setPassword("");

        mockMvc.perform(post("/customer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Validation error")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(7)))
        ;
    }

    @Test
    @DisplayName("Should except email pattern validation error while create customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void createCustomerEmailValidationErrorTest() throws Exception {
        Customer customer = new Customer()
                .setEmail("notvalid@titsonfire")
                .setFullName("Maxim Morev")
                .setAddress("Email is okay")
                .setPostcode("111123")
                .setCity("Moscow")
                .setCountry("Russia")
                .setPassword("helloFreakBitches");

        mockMvc.perform(post("/customer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(customer).toJsonString()))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Validation error")))
                .andExpect(jsonPath("$.errors[0].field", is("email")))
                .andExpect(jsonPath("$.errors[0].message", is("Invalid email address format")));
    }

    @Test
    @DisplayName("Should update customer info from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void updateCustomerTest() throws Exception {
        Optional<Customer> customer = customerService.findByEmail("test@titsonfire.store");
        Customer i = customer.get();
        assertEquals("Russia", i.getCountry());
        assertEquals("Moscow", i.getCity());
        i.setCountry("Canada");
        i.setCity("Toronto");
        mockMvc.perform(put("/update/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CustomerDto.of(i).toJsonString())
                .with(user("test@titsonfire.store")
                        .password("customer")
                        .authorities((GrantedAuthority) () -> "CUSTOMER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Toronto")))
                .andExpect(jsonPath("$.country", is("Canada")));

    }

    @Test
    @DisplayName("Should verify customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void verifyCustomerTest() throws Exception {
        CustomerVerify cv = new CustomerVerify();
        cv.setId(15L);
        cv.setVerifyCode("TKYOC");
        mockMvc.perform(post("/customer/verify/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cv.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)));
    }

    @Test
    @DisplayName("Should fail verify customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void verifyCustomerFailTest() throws Exception {
        CustomerVerify cv = new CustomerVerify();
        cv.setId(15L);
        cv.setVerifyCode("FAILy");//incorrect verify code
        mockMvc.perform(post("/customer/verify/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cv.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(false)));
    }

    @Test
    @DisplayName("Should expect error while verify customer from RequestBody")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void verifyCustomerUserNotFoundTest() throws Exception {
        CustomerVerify cv = new CustomerVerify();
        cv.setId(16L);
        cv.setVerifyCode("FAILy");//incorrect verify code
        mockMvc.perform(post("/customer/verify/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cv.toJsonString()))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Customer with id 16 not found")));
    }

    @Test
    @DisplayName("Should expect find customer by email")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void findByEmailTest() throws Exception {
        mockMvc.perform(get("/customer/email/test@titsonfire.store"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@titsonfire.store")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Should expect error while find customer by email")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void findByEmailErrorTest() throws Exception {
        mockMvc.perform(get("/customer/email/test2@titsonfire.store"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("User with test2@titsonfire.store email not found")))
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("Should expect find customer by id")
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void findByIdTest() throws Exception {
        mockMvc.perform(get("/customer/id/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@titsonfire.store")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void generateResetPasswordCode() throws Exception {
        mockMvc.perform(get("/customer/reset-password-code/id/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.resetPasswordCode", notNullValue()))
        ;
    }

    @Test
    @SqlGroup({
            @Sql(value = "classpath:db/customer/test-data.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "classpath:db/customer/clean-up.sql",
                    config = @SqlConfig(encoding = "utf-8", separator = ";", commentPrefix = "--"),
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    })
    public void updatePassword() throws Exception {
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest()
                .setNewPassword("newPassword")
                .setCustomerId(15L)
                .setResetPasswordCode(UUID.fromString("f6d56466-b345-11eb-8529-0242ac130003"));
        mockMvc.perform(post("/customer/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePasswordRequest.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.resetPasswordCode", nullValue()))
                .andExpect(jsonPath("$.password", notNullValue()))

        ;
    }


}

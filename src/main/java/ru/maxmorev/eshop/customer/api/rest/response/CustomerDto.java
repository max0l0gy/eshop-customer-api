package ru.maxmorev.eshop.customer.api.rest.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.maxmorev.eshop.customer.api.entities.Customer;
import ru.maxmorev.eshop.customer.api.entities.CustomerAuthority;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static ru.maxmorev.eshop.customer.api.DateUtil.getCurrentTimestampInMilli;

@Data
@Accessors(chain = true)
public class CustomerDto {
    private static  ObjectMapper mapper = new ObjectMapper();
    private static final String emailRFC2822 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    protected Long id;

    @Email(regexp = emailRFC2822, message = "{validation.email.format}")
    @NotBlank(message = "{validation.customer.email}")
    private String email;

    @NotBlank(message = "{validation.customer.fullName}")
    private String fullName;

    @NotBlank(message = "{validation.customer.country}")
    private String country;

    @NotBlank(message = "{validation.customer.postcode}")
    private String postcode;

    @NotBlank(message = "{validation.customer.city}")
    private String city;
    
    @NotBlank(message = "{validation.customer.address}")
    private String address;

    private Date dateOfCreation;

    private String password;

    private Boolean verified = false;

    private String verifyCode;

    private Long shoppingCartId;

    private UUID resetPasswordCode;

    private Long resetPasswordCodeGeneratedTimestamp;

    private Long currentTimestamp;

    private Collection<CustomerAuthority> authorities;

    public static CustomerDto of(Customer info){
        return new CustomerDto()
                .setId(info.getId())
                .setEmail(info.getEmail())
                .setFullName(info.getFullName())
                .setCountry(info.getCountry())
                .setCity(info.getCity())
                .setAddress(info.getAddress())
                .setPostcode(info.getPostcode())
                .setVerified(info.getVerified())
                .setShoppingCartId(info.getShoppingCartId())
                .setResetPasswordCode(info.getResetPasswordCode())
                .setResetPasswordCodeGeneratedTimestamp(info.getResetPasswordCodeGeneratedTimestamp())
                .setPassword(info.getPassword())
                .setVerifyCode(info.getVerifyCode())
                .setDateOfCreation(info.getDateOfCreation())
                .setAuthorities(( Collection<CustomerAuthority>)info.getAuthorities())
                .setCurrentTimestamp(getCurrentTimestampInMilli())
                ;
    }
    
    public static Customer from(CustomerDto customerDto) {
        return new Customer()
                .setId(customerDto.getId())
                .setEmail(customerDto.getEmail())
                .setFullName(customerDto.getFullName())
                .setCountry(customerDto.getCountry())
                .setCity(customerDto.getCity())
                .setAddress(customerDto.getAddress())
                .setPostcode(customerDto.getPostcode())
                .setVerified(customerDto.getVerified())
                .setShoppingCartId(customerDto.getShoppingCartId())
                .setResetPasswordCode(customerDto.getResetPasswordCode())
                .setResetPasswordCodeGeneratedTimestamp(customerDto.getResetPasswordCodeGeneratedTimestamp())
                .setPassword(customerDto.getPassword())
                .setDateOfCreation(customerDto.getDateOfCreation())
                .setVerifyCode(customerDto.getVerifyCode())
                ;
    }

    public String toJsonString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}

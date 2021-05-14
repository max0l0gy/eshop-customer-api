package ru.maxmorev.eshop.customer.api.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerVerify extends JsonMappedValue {
    private static ObjectMapper mapper = new ObjectMapper();
    @NotNull(message = "{validation.customerVerify.id}")
    private Long id;
    @NotBlank(message = "{validation.customerVerify.verifyCode}")
    private String verifyCode;
    private Boolean verified;
}

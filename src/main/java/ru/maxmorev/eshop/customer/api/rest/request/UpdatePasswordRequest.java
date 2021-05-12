package ru.maxmorev.eshop.customer.api.rest.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class UpdatePasswordRequest extends JsonMappedValue {
    @NotBlank
    private String newPassword;
    @NotNull
    private Long customerId;
    @NotBlank
    private UUID resetPasswordCode;
}

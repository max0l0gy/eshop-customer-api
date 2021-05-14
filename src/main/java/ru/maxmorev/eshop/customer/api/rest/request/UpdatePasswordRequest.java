package ru.maxmorev.eshop.customer.api.rest.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UpdatePasswordRequest extends JsonMappedValue {
    @NotBlank
    private String newPassword;
    @NotNull
    private Long customerId;
    @NotNull
    private UUID resetPasswordCode;
}

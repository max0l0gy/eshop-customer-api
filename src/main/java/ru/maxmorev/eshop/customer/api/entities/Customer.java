package ru.maxmorev.eshop.customer.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.maxmorev.eshop.customer.api.annotation.AuthorityValues;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "customer")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer implements UserDetails {

    private static final String emailRFC2822 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR_CUSTOMER)
    @Column(updatable = false)
    protected Long id;

    @Email(regexp = emailRFC2822, message = "{validation.email.format}")
    @NotBlank(message = "{validation.customer.email}")
    @Column(nullable = false, length = 256, unique = true)
    private String email;

    @NotBlank(message = "{validation.customer.fullName}")
    @Column(name = "fullname", nullable = false, length = 256)
    private String fullName;

    @NotBlank(message = "{validation.customer.country}")
    @Column(nullable = false, length = 256)
    private String country;

    @NotBlank(message = "{validation.customer.postcode}")
    @Column(nullable = false, length = 256)
    private String postcode;

    @NotBlank(message = "{validation.customer.city}")
    @Column(nullable = false, length = 256)
    private String city;

    @NotBlank(message = "{validation.customer.address}")
    @Column(nullable = false, length = 256)
    private String address;

    @NotBlank(message = "{validation.customer.password}")
    @Column(nullable = false, length = 256)
    private String password;

    @Column(name = "verifycode", nullable = false, length = 256)
    private String verifyCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_creation", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Date dateOfCreation;

    @Column
    private Boolean verified = false;

    @Column(name = "shopping_cart_id")
    private Long shoppingCartId;

    @Column(name = "authorities", nullable = false)
    private String authorities;

    @Type(type="uuid-char")
    @Column(name = "reset_password_code")
    private UUID resetPasswordCode;

    @Column(name="reset_password_code_generated_timestamp")
    private Long resetPasswordCodeGeneratedTimestamp;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<CustomerAuthority> authSet = new HashSet<>();
        if (Objects.isNull(authorities))
            return null;
        Arrays.asList(authorities.split(","))
                .forEach(str ->
                        authSet.add(new CustomerAuthority(AuthorityValues.valueOf(str))));
        return authSet;
    }

    public void addAuthority(AuthorityValues auth) {
        if (Objects.isNull(authorities)) {
            authorities = auth.name();
            return;
        }
        Collection<? extends GrantedAuthority> authSet = getAuthorities();
        if (authSet.contains(new CustomerAuthority(auth))) return;
        authorities += "," + auth.name();
    }

    public void removeAllAuthorities() {
        this.authorities = null;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return verified;
    }

    @Override
    public boolean isAccountNonLocked() {
        return verified;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return verified;
    }

    @Override
    public boolean isEnabled() {
        return verified;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}

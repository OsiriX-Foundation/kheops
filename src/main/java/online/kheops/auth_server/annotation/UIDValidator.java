package online.kheops.auth_server.annotation;

import online.kheops.auth_server.validator.UIDConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NameBinding
@Retention(RUNTIME)
@Target(PARAMETER)
@Constraint(validatedBy = UIDConstraintValidator.class)
public @interface UIDValidator {
    String message() default "{constraint.UIDValidator}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

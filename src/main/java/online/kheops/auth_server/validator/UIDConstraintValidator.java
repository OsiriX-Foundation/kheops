package online.kheops.auth_server.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UIDConstraintValidator implements ConstraintValidator<String, String> {

    @Override
    public void initialize(String constraintAnnotation) {
        
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return false;
    }
}

package sit.int221.kanbanapi.validates;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueStatusNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueStatusName {
    String message() default "Status name must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
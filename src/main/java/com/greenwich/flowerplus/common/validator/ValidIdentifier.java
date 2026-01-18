package com.greenwich.flowerplus.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IdentifierValidator.class) // Trỏ tới class xử lý logic
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIdentifier {

    String message() default "Invalid Email or Username";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

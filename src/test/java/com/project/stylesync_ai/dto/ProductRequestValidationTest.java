package com.project.stylesync_ai.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ProductRequest requestWithPrice(BigDecimal price) {
        ProductRequest request = new ProductRequest();
        request.setName("Black T-Shirt");
        request.setPrice(price);
        return request;
    }

    private boolean hasViolationOnPrice(Set<ConstraintViolation<ProductRequest>> violations) {
        return violations.stream()
                .anyMatch(v -> "price".equals(v.getPropertyPath().toString()));
    }

    @Test
    void shouldRejectMissingPrice() {
        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(requestWithPrice(null));

        assertTrue(hasViolationOnPrice(violations));
        assertTrue(violations.stream().anyMatch(
                v -> "Price is required".equals(v.getMessage())));
    }

    @Test
    void shouldRejectNegativePrice() {
        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(requestWithPrice(new BigDecimal("-1.00")));

        assertTrue(hasViolationOnPrice(violations));
    }

    @Test
    void shouldRejectMoreThanTwoDecimalPlaces() {
        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(requestWithPrice(new BigDecimal("799.999")));

        assertTrue(hasViolationOnPrice(violations));
    }

    @Test
    void shouldAcceptZeroPrice() {
        assertEquals(0, validator.validate(
                requestWithPrice(BigDecimal.ZERO)).size());
    }

    @Test
    void shouldAcceptValidPrice() {
        assertEquals(0, validator.validate(
                requestWithPrice(new BigDecimal("799.00"))).size());
    }
}

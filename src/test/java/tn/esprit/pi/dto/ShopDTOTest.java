package tn.esprit.pi.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.pi.domain.SportType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void productRequest_ShouldHaveNoViolations_WhenValid() {
        ShopDTOs.ProductRequest request = new ShopDTOs.ProductRequest(
                "Football", 25.0, 10, "Equipment", "img.png", SportType.FOOTBALL
        );
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void productRequest_ShouldHaveViolations_WhenPriceIsNegative() {
        ShopDTOs.ProductRequest request = new ShopDTOs.ProductRequest(
                "Football", -5.0, 10, "Equipment", "img.png", SportType.FOOTBALL
        );
        assertFalse(validator.validate(request).isEmpty());
    }
}
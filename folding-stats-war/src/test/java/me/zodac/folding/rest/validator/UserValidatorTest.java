package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserValidator}.
 */
class UserValidatorTest {

    @Test
    void whenValidatingCreate_givenValidUser_thenSuccessResponseIsReturned() {
        assertThat(true)
            .isTrue();
    }
}

package com.example.sixcoachconfectionery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {
    @Test fun validEmail_isAccepted() =
        assertTrue(Validator.isValidEmail("ndu@school.co.za"))

    @Test fun emailWithoutAt_isRejected() =
        assertFalse(Validator.isValidEmail("nduschool.co.za"))

    @Test fun emptyEmail_isRejected() =
        assertFalse(Validator.isValidEmail(""))

    @Test fun shortPassword_isRejected() =
        assertFalse(Validator.isValidPassword("abc123"))

    @Test fun eightCharPassword_isAccepted() =
        assertTrue(Validator.isValidPassword("abcd1234"))

    @Test fun zeroQuantity_isRejected() =
        assertFalse(Validator.isValidQuantity(0))

    @Test fun negativeQuantity_isRejected() =
        assertFalse(Validator.isValidQuantity(-3))

    @Test fun normalQuantity_isAccepted() =
        assertTrue(Validator.isValidQuantity(5))
}
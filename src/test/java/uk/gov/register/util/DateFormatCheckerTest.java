package uk.gov.register.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateFormatCheckerTest {

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYY() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYDDMM() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDD() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmss() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01T01:01:01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmssZ() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01T01:01:01Z"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsNotValid() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid("2012/01/01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsEmpty() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid(""));
    }

    @Test
    public void validateDateOrder_shouldValidateSuccessfully_whenInputDatesAreOrdered() throws IOException {
        assertTrue("Dates should be ordered", DateFormatChecker.isDateTimeFormatsOrdered("2012-01-01T01:01:01Z", "2012-01-01T01:01:02Z"));
    }

    @Test
    public void validateDateOrder_shouldNotValidate_whenInputDatesAreNotOrdered() throws IOException {
        assertFalse("Dates should not be ordered", DateFormatChecker.isDateTimeFormatsOrdered("2012-01-01T01:01:02Z", "2012-01-01T01:01:01Z"));
    }
}

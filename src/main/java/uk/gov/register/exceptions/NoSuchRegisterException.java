package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

/**
 * Used if the definition of a particular register is missing from the register
 */
public class NoSuchRegisterException extends RuntimeException {
    public NoSuchRegisterException(RegisterId registerId) {
        super(String.format("Definition for %s register is missing", registerId.value()));
    }
}

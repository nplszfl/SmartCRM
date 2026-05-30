package com.smartcrm.contract.exception;

import com.smartcrm.common.exception.BusinessException;

/**
 * Exception for duplicate contract number.
 */
public class DuplicateContractNumberException extends BusinessException {

    public DuplicateContractNumberException(String contractNumber) {
        super(409, "Contract with number already exists: " + contractNumber);
    }
}
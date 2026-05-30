package com.smartcrm.contract.exception;

import com.smartcrm.common.exception.BusinessException;

/**
 * Exception for invalid contract status transitions.
 */
public class ContractStatusException extends BusinessException {

    public ContractStatusException(String message) {
        super(400, message);
    }

    public ContractStatusException(String fromStatus, String toStatus) {
        super(400, "Cannot transition contract from " + fromStatus + " to " + toStatus);
    }
}
package com.sonexa.backend.security;

import com.sonexa.backend.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductionSecurityContractTest {

    @Test
    void forbiddenErrorCodeExistsForAuthorizationFailures() {
        assertEquals("ERR_40301", ErrorCode.FORBIDDEN.getCode());
        assertNotNull(ErrorCode.UNAUTHORIZED.getCode());
        assertNotNull(ErrorCode.INVALID_TOKEN.getCode());
    }
}

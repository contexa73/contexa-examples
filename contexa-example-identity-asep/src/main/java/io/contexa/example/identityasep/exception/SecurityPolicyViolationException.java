package io.contexa.example.identityasep.exception;

/**
 * Custom exception thrown within the security filter chain
 * to demonstrate ASEP exception handling.
 *
 * Represents a security policy violation detected by a custom filter.
 */
public class SecurityPolicyViolationException extends RuntimeException {

    private final String policyCode;
    private final String detail;

    public SecurityPolicyViolationException(String policyCode, String message, String detail) {
        super(message);
        this.policyCode = policyCode;
        this.detail = detail;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public String getDetail() {
        return detail;
    }
}

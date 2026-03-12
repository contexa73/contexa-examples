package io.contexa.example.iamprotectableanalysis.service;

import io.contexa.contexacommon.annotation.Protectable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Security flow test service.
 *
 * Each method is protected by @Protectable annotation.
 * Policies are loaded from database and evaluated by the method security interceptor.
 *
 * Method identifier format: {package}.{class}.{method}({parameterTypes})
 */
@Slf4j
@Service
public class TestSecurityService {

    /**
     * Public data - analysis not required.
     * AnalysisRequirement.NOT_REQUIRED
     */
    @Protectable
    public String getPublicData(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId is required.");
        }
        return String.format("Public data [%s]: accessible to all authenticated users.", resourceId);
    }

    /**
     * Normal data - analysis preferred.
     * AnalysisRequirement.PREFERRED
     */
    @Protectable
    public String getNormalData(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId is required.");
        }
        return String.format("Normal data [%s]: accessible when ALLOW action.", resourceId);
    }

    /**
     * Sensitive data - analysis required.
     * AnalysisRequirement.REQUIRED
     */
    @Protectable
    public String getSensitiveData(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId is required.");
        }
        return String.format("Sensitive data [%s]: accessible when LLM analysis completed with ALLOW/MONITOR.", resourceId);
    }

    /**
     * Critical data - ALLOW only.
     * AnalysisRequirement.STRICT
     */
    @Protectable
    public String getCriticalData(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId is required.");
        }
        return String.format("Critical data [%s]: ADMIN + LLM ALLOW action only.", resourceId);
    }

    /**
     * Bulk data - runtime interception enabled.
     * AnalysisRequirement.PREFERRED + enableRuntimeInterception
     */
    @Protectable
    public String getBulkData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bulk data query result:\n");
        for (int i = 1; i <= 10000; i++) {
            sb.append(String.format("Record-%05d: data item %d\n", i, i));
        }
        sb.append("Total 10,000 records retrieved.");
        return sb.toString();
    }
}

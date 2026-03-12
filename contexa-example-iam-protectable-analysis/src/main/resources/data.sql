-- ============================================================================
-- @Protectable Method-Level Policies
-- ============================================================================
--
-- Policies for TestSecurityService @Protectable methods.
-- Each method has a different AnalysisRequirement level.
--
-- #trust SpEL expressions reference:
-- - hasAction(String)                     - Check specific action
-- - hasActionIn(String...)                - Check action in allowed list
-- - requiresAnalysisWithAction(String...) - Analysis completed + action check
-- - hasActionOrDefault(String, String...) - Default action when pending
-- ============================================================================


-- 1. Policies
INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(10001, 'TEST_PUBLIC_DATA_ACCESS',
 'Public data - authenticated users only, no AI analysis',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(10002, 'TEST_NORMAL_DATA_ACCESS',
 'Normal data - ALLOW or MONITOR action with USER role',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(10003, 'TEST_SENSITIVE_DATA_ACCESS',
 'Sensitive data - AI analysis must complete with ALLOW or MONITOR',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(10004, 'TEST_CRITICAL_DATA_ACCESS',
 'Critical data - ADMIN role + AI analysis ALLOW only',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(10005, 'TEST_BULK_DATA_ACCESS',
 'Bulk data - BLOCK action rejected, MONITOR default when pending',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);


-- 2. Policy Rules
INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(10001, 10001, 'Public data rule - authentication check'),
(10002, 10002, 'Normal data rule - action-based access control'),
(10003, 10003, 'Sensitive data rule - analysis completion required'),
(10004, 10004, 'Critical data rule - ADMIN + ALLOW only'),
(10005, 10005, 'Bulk data rule - default MONITOR allowed');


-- 3. Policy Conditions (SpEL)
INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(10001, 10001,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users only - no AI action check'),

(10002, 10002,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'ALLOW or MONITOR action required with USER role'),

(10003, 10003,
 '#trust.requiresAnalysisWithAction(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'AI analysis must complete with ALLOW or MONITOR action'),

(10004, 10004,
 'hasRole(''ADMIN'') and #trust.requiresAnalysisWithAction(''ALLOW'')',
 'PRE_AUTHORIZE',
 'ADMIN role + AI analysis ALLOW action only'),

(10005, 10005,
 '#trust.hasActionOrDefault(''MONITOR'', ''ALLOW'', ''MONITOR'')',
 'PRE_AUTHORIZE',
 'BLOCK rejected - MONITOR default when analysis pending');


-- 4. Policy Targets (method mapping)
INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(10001, 10001, 'METHOD',
 'io.contexa.example.iamprotectableanalysis.service.TestSecurityService.getPublicData(String)',
 'ANY'),

(10002, 10002, 'METHOD',
 'io.contexa.example.iamprotectableanalysis.service.TestSecurityService.getNormalData(String)',
 'ANY'),

(10003, 10003, 'METHOD',
 'io.contexa.example.iamprotectableanalysis.service.TestSecurityService.getSensitiveData(String)',
 'ANY'),

(10004, 10004, 'METHOD',
 'io.contexa.example.iamprotectableanalysis.service.TestSecurityService.getCriticalData(String)',
 'ANY'),

(10005, 10005, 'METHOD',
 'io.contexa.example.iamprotectableanalysis.service.TestSecurityService.getBulkData()',
 'ANY');

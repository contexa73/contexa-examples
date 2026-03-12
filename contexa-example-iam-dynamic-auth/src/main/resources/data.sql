-- ============================================================================
-- URL Dynamic Authorization Policies
-- ============================================================================
--
-- Demonstrates URL-level policies with AI security expressions.
-- CustomDynamicAuthorizationManager loads these at startup and evaluates
-- them for every incoming HTTP request.
--
-- AI Security Expression Reference:
-- #trust (Hot Path - Redis/InMemory cached):
--   .hasAction(String)                    - Check specific action
--   .hasActionIn(String...)               - Check action in allowed list
--   .isAllowed()                          - Action is ALLOW
--   .isBlocked()                          - Action is BLOCK
--   .requiresAnalysisWithAction(String...) - Analysis completed + action check
--   .hasActionOrDefault(String, String...) - Default action when pending
--
-- Standard SpEL:
--   isAuthenticated()                     - User is authenticated
--   hasRole(String)                       - User has role
--   hasAuthority(String)                  - User has authority
-- ============================================================================


-- ============================================================================
-- 1. Policies
-- ============================================================================

INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
-- Public resources: authentication only
(20001, 'URL_PUBLIC_RESOURCE_ACCESS',
 'Public resource access - authenticated users only, no AI analysis',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

-- Normal resources: ALLOW or MONITOR action
(20002, 'URL_NORMAL_RESOURCE_ACCESS',
 'Normal resource access - ALLOW or MONITOR action with USER role',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

-- Sensitive resources: analysis required
(20003, 'URL_SENSITIVE_RESOURCE_ACCESS',
 'Sensitive resource access - AI analysis must complete with ALLOW or MONITOR',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

-- Admin resources: ADMIN + strict ALLOW only
(20004, 'URL_ADMIN_RESOURCE_ACCESS',
 'Admin resource access - ADMIN role + AI analysis ALLOW only',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);


-- ============================================================================
-- 2. Policy Rules
-- ============================================================================

INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(20001, 20001, 'Public resource rule - authentication check'),
(20002, 20002, 'Normal resource rule - action-based access control'),
(20003, 20003, 'Sensitive resource rule - analysis completion required'),
(20004, 20004, 'Admin resource rule - ADMIN + strict ALLOW');


-- ============================================================================
-- 3. Policy Conditions (SpEL with AI expressions)
-- ============================================================================

INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
-- Public: authentication only
(20001, 20001,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users only - no AI action check'),

-- Normal: ALLOW or MONITOR action + USER role
(20002, 20002,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'ALLOW or MONITOR action required with USER role'),

-- Sensitive: analysis must complete + ALLOW/MONITOR
(20003, 20003,
 '#trust.requiresAnalysisWithAction(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'AI analysis must complete with ALLOW or MONITOR action'),

-- Admin: ADMIN role + analysis ALLOW only
(20004, 20004,
 'hasRole(''ADMIN'') and #trust.requiresAnalysisWithAction(''ALLOW'')',
 'PRE_AUTHORIZE',
 'ADMIN role + AI analysis ALLOW action only');


-- ============================================================================
-- 4. Policy Targets (URL mapping)
-- ============================================================================

INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(20001, 20001, 'URL', '/api/resources/public/**', 'ANY'),
(20002, 20002, 'URL', '/api/resources/normal/**', 'ANY'),
(20003, 20003, 'URL', '/api/resources/sensitive/**', 'ANY'),
(20004, 20004, 'URL', '/api/resources/admin/**', 'ANY');

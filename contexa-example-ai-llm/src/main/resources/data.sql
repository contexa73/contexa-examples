-- ============================================================================
-- AI LLM Example - URL Dynamic Authorization Policies
-- ============================================================================

INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(32001, 'URL_LLM_PUBLIC_ACCESS',
 'Public access for LLM static resources and health check',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(32002, 'URL_LLM_API_ACCESS',
 'LLM API access - authenticated users with ALLOW or MONITOR action',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(32003, 'URL_LLM_PAGE_ACCESS',
 'LLM test page access - authenticated users only',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);


INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(32001, 32001, 'Public resource rule'),
(32002, 32002, 'LLM API rule - action-based access'),
(32003, 32003, 'LLM page rule - authentication check');


INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(32001, 32001,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users only'),

(32002, 32002,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'ALLOW or MONITOR action with USER role'),

(32003, 32003,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users for test pages');


INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(32001, 32001, 'URL', '/api/health', 'ANY'),
(32002, 32002, 'URL', '/api/llm/**', 'ANY'),
(32003, 32003, 'URL', '/llm/**', 'ANY');

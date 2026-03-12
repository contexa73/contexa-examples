-- ============================================================================
-- AI Lab Example - URL Dynamic Authorization Policies
-- ============================================================================

INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(30001, 'URL_LAB_PUBLIC_ACCESS',
 'Public access for lab static resources and health check',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(30002, 'URL_LAB_API_ACCESS',
 'Lab API access - authenticated users with ALLOW or MONITOR action',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(30003, 'URL_LAB_PAGE_ACCESS',
 'Lab test page access - authenticated users only',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);


INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(30001, 30001, 'Public resource rule'),
(30002, 30002, 'Lab API rule - action-based access'),
(30003, 30003, 'Lab page rule - authentication check');


INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(30001, 30001,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users only'),

(30002, 30002,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'ALLOW or MONITOR action with USER role'),

(30003, 30003,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users for test pages');


INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(30001, 30001, 'URL', '/api/health', 'ANY'),
(30002, 30002, 'URL', '/api/lab/**', 'ANY'),
(30003, 30003, 'URL', '/lab/**', 'ANY');

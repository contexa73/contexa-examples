-- ============================================================================
-- AI Pipeline Example - URL Dynamic Authorization Policies
-- ============================================================================

INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(31001, 'URL_PIPELINE_PUBLIC_ACCESS',
 'Public access for pipeline static resources and health check',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(31002, 'URL_PIPELINE_API_ACCESS',
 'Pipeline API access - authenticated users with ALLOW or MONITOR action',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP),

(31003, 'URL_PIPELINE_PAGE_ACCESS',
 'Pipeline test page access - authenticated users only',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);


INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(31001, 31001, 'Public resource rule'),
(31002, 31002, 'Pipeline API rule - action-based access'),
(31003, 31003, 'Pipeline page rule - authentication check');


INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(31001, 31001,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users only'),

(31002, 31002,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and hasRole(''USER'')',
 'PRE_AUTHORIZE',
 'ALLOW or MONITOR action with USER role'),

(31003, 31003,
 'isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users for test pages');


INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(31001, 31001, 'URL', '/api/health', 'ANY'),
(31002, 31002, 'URL', '/api/pipeline/**', 'ANY'),
(31003, 31003, 'URL', '/pipeline/**', 'ANY');

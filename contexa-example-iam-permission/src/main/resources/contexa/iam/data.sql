-- ============================================================================
-- URL Policy for document API access
-- ============================================================================

INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(30001, 'DOCUMENT_API_ACCESS',
 'Document API - authenticated users with ALLOW or MONITOR action',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(30001, 30001, 'Document API rule - AI trust check + authentication')
ON CONFLICT (id) DO NOTHING;

INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(30001, 30001,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users with ALLOW or MONITOR AI action')
ON CONFLICT (id) DO NOTHING;

INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method, target_order)
VALUES
(30001, 30001, 'URL', '/api/documents/**', 'ANY', 0)
ON CONFLICT (id) DO NOTHING;

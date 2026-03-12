-- ============================================================================
-- Permission Evaluator Example Data
-- ============================================================================
--
-- 1. Document sample data for hasPermission() demonstration
-- 2. URL policies for /api/documents/** endpoints
--
-- Permission rules (implemented in DocumentPermissionEvaluator):
-- - ADMIN: full access to all documents
-- - Owner (ownerId == username): full CRUD on own documents
-- - USER: READ-only access
-- ============================================================================


-- 1. Document sample data
INSERT INTO DOCUMENT (id, title, owner_id, department_id, security_level) VALUES
(1, 'Public Report Q4', 'admin', 'engineering', 'PUBLIC'),
(2, 'User Manual v2.1', 'user', 'engineering', 'INTERNAL'),
(3, 'Financial Report 2024', 'admin', 'finance', 'CONFIDENTIAL'),
(4, 'API Design Spec', 'user', 'engineering', 'INTERNAL'),
(5, 'HR Policy Document', 'admin', 'hr', 'RESTRICTED');


-- 2. URL Policy for document API access
INSERT INTO POLICY (id, name, description, effect, priority, is_active, source, approval_status, created_at)
VALUES
(30001, 'DOCUMENT_API_ACCESS',
 'Document API - authenticated users with ALLOW or MONITOR action',
 'ALLOW', 100, true, 'MANUAL', 'NOT_REQUIRED', CURRENT_TIMESTAMP);

INSERT INTO POLICY_RULE (id, policy_id, description)
VALUES
(30001, 30001, 'Document API rule - AI trust check + authentication');

INSERT INTO POLICY_CONDITION (id, rule_id, condition_expression, authorization_phase, description)
VALUES
(30001, 30001,
 '#trust.hasActionIn(''ALLOW'', ''MONITOR'') and isAuthenticated()',
 'PRE_AUTHORIZE',
 'Authenticated users with ALLOW or MONITOR AI action');

INSERT INTO POLICY_TARGET (id, policy_id, target_type, target_identifier, http_method)
VALUES
(30001, 30001, 'URL', '/api/documents/**', 'ANY');

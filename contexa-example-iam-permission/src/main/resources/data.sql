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

package io.contexa.example.iampermission.evaluator;

import io.contexa.contexaiam.security.xacml.pdp.evaluation.method.DomainPermissionEvaluator;
import io.contexa.example.iampermission.domain.Document;
import io.contexa.example.iampermission.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

/**
 * Custom DomainPermissionEvaluator for Document domain.
 *
 * Automatically registered with CompositePermissionEvaluator via Spring component scan.
 * Evaluated when @PreAuthorize("hasPermission(#id, 'DOCUMENT', 'READ')") is used.
 *
 * Permission logic:
 * - ADMIN role: all permissions on all documents
 * - Owner (ownerId == username): full access (READ, UPDATE, DELETE)
 * - USER role: READ-only access
 * - Others: no access
 */
@Component
@RequiredArgsConstructor
public class DocumentPermissionEvaluator implements DomainPermissionEvaluator {

    private static final Set<String> SUPPORTED_PERMISSIONS = Set.of("READ", "CREATE", "UPDATE", "DELETE");

    private final DocumentRepository documentRepository;

    @Override
    public boolean supportsTargetType(String targetType) {
        return "DOCUMENT".equalsIgnoreCase(targetType);
    }

    @Override
    public boolean supportsPermission(String permission) {
        if (permission == null) {
            return false;
        }
        return SUPPORTED_PERMISSIONS.contains(permission.toUpperCase());
    }

    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // ADMIN: all permissions
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        if (target instanceof Document doc) {
            return evaluateDocumentPermission(auth, doc, permission.toString());
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // ADMIN: all permissions
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        // CREATE does not require existing entity
        if ("CREATE".equalsIgnoreCase(permission.toString())) {
            return hasRole(auth, "USER");
        }

        // Resolve entity and evaluate
        Document doc = documentRepository.findById((Long) targetId).orElse(null);
        if (doc == null) {
            return false;
        }

        return evaluateDocumentPermission(auth, doc, permission.toString());
    }

    @Override
    public Object resolveEntity(Serializable targetId) {
        return documentRepository.findById((Long) targetId).orElse(null);
    }

    private boolean evaluateDocumentPermission(Authentication auth, Document doc, String permission) {
        String username = auth.getName();
        String perm = permission.toUpperCase();

        // Owner: full access
        if (username.equals(doc.getOwnerId())) {
            return true;
        }

        // USER role: READ only
        if ("READ".equals(perm) && hasRole(auth, "USER")) {
            return true;
        }

        return false;
    }

    private boolean hasRole(Authentication auth, String role) {
        String roleWithPrefix = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(roleWithPrefix) || a.equals(role));
    }
}

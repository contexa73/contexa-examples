package io.contexa.example.iampermission.controller;

import io.contexa.example.iampermission.domain.Document;
import io.contexa.example.iampermission.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Document CRUD controller with hasPermission() authorization.
 *
 * Each endpoint uses @PreAuthorize with hasPermission() expressions.
 * CompositePermissionEvaluator routes to DocumentPermissionEvaluator
 * based on targetType "DOCUMENT".
 *
 * Permission matrix:
 * - GET    /api/documents      : authenticated (list all)
 * - GET    /api/documents/{id} : hasPermission(#id, 'DOCUMENT', 'READ')
 * - POST   /api/documents      : hasPermission(null, 'DOCUMENT', 'CREATE')
 * - PUT    /api/documents/{id} : hasPermission(#id, 'DOCUMENT', 'UPDATE')
 * - DELETE /api/documents/{id} : hasPermission(#id, 'DOCUMENT', 'DELETE')
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;

    @GetMapping
    public List<Document> listDocuments() {
        return documentRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'DOCUMENT', 'READ')")
    public ResponseEntity<Map<String, Object>> getDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> ResponseEntity.ok(buildDocumentResponse(doc, "READ")))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'DOCUMENT', 'CREATE')")
    public ResponseEntity<Map<String, Object>> createDocument(@RequestBody Document document) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        document.setOwnerId(auth.getName());
        Document saved = documentRepository.save(document);
        return ResponseEntity.ok(buildDocumentResponse(saved, "CREATE"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'DOCUMENT', 'UPDATE')")
    public ResponseEntity<Map<String, Object>> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        return documentRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(document.getTitle());
                    existing.setDepartmentId(document.getDepartmentId());
                    existing.setSecurityLevel(document.getSecurityLevel());
                    Document updated = documentRepository.save(existing);
                    return ResponseEntity.ok(buildDocumentResponse(updated, "UPDATE"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'DOCUMENT', 'DELETE')")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    documentRepository.delete(doc);
                    return ResponseEntity.ok(buildDocumentResponse(doc, "DELETE"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> buildDocumentResponse(Document doc, String operation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "operation", operation,
                "document", doc,
                "requestedBy", auth.getName(),
                "isOwner", auth.getName().equals(doc.getOwnerId())
        );
    }
}

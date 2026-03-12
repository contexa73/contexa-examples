package io.contexa.example.iampermission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document domain entity for permission evaluation example.
 *
 * Permission rules:
 * - ADMIN: full access to all documents
 * - Owner (ownerId matches username): full access to own documents
 * - USER role: READ-only access to documents in same department
 */
@Entity
@Table(name = "document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Column(name = "security_level")
    private String securityLevel;
}

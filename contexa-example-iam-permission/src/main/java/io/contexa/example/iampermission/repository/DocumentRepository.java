package io.contexa.example.iampermission.repository;

import io.contexa.example.iampermission.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerId(String ownerId);

    List<Document> findByDepartmentId(String departmentId);
}

# Claim Service Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter des tests unitaires (Mockito) et d'intégration (SpringBootTest + H2) pour la logique métier du `ClaimService`.

**Architecture:** Tests unitaires avec `@ExtendWith(MockitoExtension.class)` — repos mockés, logique isolée. Tests d'intégration avec `@SpringBootTest` + H2 en mémoire — vrai contexte Spring, rollback automatique via `@Transactional`.

**Tech Stack:** JUnit 5, Mockito, Spring Boot Test, H2 (scope test)

---

## Fichiers concernés

| Action | Chemin |
|--------|--------|
| Modifier | `backend/claim-service/pom.xml` |
| Créer | `backend/claim-service/src/test/resources/application-test.properties` |
| Créer | `backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java` |
| Créer | `backend/claim-service/src/test/java/tn/english/school/claimservice/integration/ClaimServiceIntegrationTest.java` |

---

## Task 1 : Ajouter H2 et configurer les properties de test

**Files:**
- Modify: `backend/claim-service/pom.xml`
- Create: `backend/claim-service/src/test/resources/application-test.properties`

- [ ] **Step 1 : Ajouter la dépendance H2 dans pom.xml**

Dans `backend/claim-service/pom.xml`, ajouter après la dépendance `spring-boot-starter-test` :

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2 : Créer application-test.properties**

Créer le fichier `backend/claim-service/src/test/resources/application-test.properties` :

```properties
# Base H2 en mémoire
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=false

# Désactiver Eureka en test
eureka.client.enabled=false
spring.cloud.discovery.enabled=false

# Désactiver la config Groq (pas besoin en test)
groq.api.token=fake-token-for-tests
```

- [ ] **Step 3 : Vérifier que Maven compile sans erreur**

```bash
cd backend/claim-service && mvn test-compile -q
```

Résultat attendu : aucune erreur.

- [ ] **Step 4 : Commit**

```bash
git add backend/claim-service/pom.xml backend/claim-service/src/test/resources/application-test.properties
git commit -m "test(claim): add H2 dependency and test configuration"
```

---

## Task 2 : Tests unitaires — cas de succès

**Files:**
- Create: `backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java`

- [ ] **Step 1 : Créer ClaimServiceTest avec les cas de succès**

Créer `backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java` :

```java
package tn.english.school.claimservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.entity.User;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.exception.DuplicateClaimException;
import tn.english.school.claimservice.repository.ClaimRepository;
import tn.english.school.claimservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User student;
    private Claim claim;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setFirstName("Alice");
        student.setLastName("Martin");
        student.setEmail("alice@esprit.tn");

        claim = new Claim();
        claim.setId(1L);
        claim.setSubject("Exam contestation");
        claim.setDescription("Wrong grade");
        claim.setType(ClaimType.PEDAGOGICAL);
        claim.setStatus(ClaimStatus.OPEN);
        claim.setStudent(student);
    }

    // ── createClaim ──────────────────────────────────────────────────────────

    @Test
    void createClaim_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);
        when(claimRepository.existsByStudentIdAndSubjectAndStatusIn(1L, "Exam contestation",
                List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS))).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim result = claimService.createClaim(claim);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.OPEN);
        verify(claimRepository).save(claim);
    }

    // ── authorizeRetake ───────────────────────────────────────────────────────

    @Test
    void authorizeRetake_success() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim result = claimService.authorizeRetake(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
        verify(claimRepository).save(claim);
    }

    @Test
    void authorizeRetake_alreadyAuthorized_returnsExistingWithoutSave() {
        claim.setStatus(ClaimStatus.RETAKE_AUTHORIZED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Claim result = claimService.authorizeRetake(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
        verify(claimRepository, never()).save(any());
    }
}
```

- [ ] **Step 2 : Lancer les tests et vérifier qu'ils passent**

```bash
cd backend/claim-service && mvn test -Dtest=ClaimServiceTest -q
```

Résultat attendu : `BUILD SUCCESS`, 3 tests passés.

- [ ] **Step 3 : Commit**

```bash
git add backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java
git commit -m "test(claim): add unit tests for createClaim and authorizeRetake success cases"
```

---

## Task 3 : Tests unitaires — cas d'erreur (logique métier)

**Files:**
- Modify: `backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java`

- [ ] **Step 1 : Ajouter les tests d'erreur dans ClaimServiceTest**

Ajouter ces méthodes dans la classe `ClaimServiceTest`, avant la dernière accolade `}` :

```java
    // ── createClaim — doublon ────────────────────────────────────────────────

    @Test
    void createClaim_duplicateActive_throwsDuplicateClaimException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);
        when(claimRepository.existsByStudentIdAndSubjectAndStatusIn(1L, "Exam contestation",
                List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS))).thenReturn(true);

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(DuplicateClaimException.class)
                .hasMessageContaining("Duplicate Alert");

        verify(claimRepository, never()).save(any());
    }

    // ── updateClaim ───────────────────────────────────────────────────────────

    @Test
    void updateClaim_statusNotOpen_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.IN_PROGRESS);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Claim updated = new Claim();
        updated.setSubject("New subject");
        updated.setDescription("New desc");
        updated.setType(ClaimType.TECHNICAL);

        assertThatThrownBy(() -> claimService.updateClaim(1L, updated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Action denied");

        verify(claimRepository, never()).save(any());
    }

    // ── deleteClaim ───────────────────────────────────────────────────────────

    @Test
    void deleteClaim_statusNotOpen_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.RESOLVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.deleteClaim(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Action denied");

        verify(claimRepository, never()).deleteById(any());
    }

    // ── authorizeRetake — cas d'erreur ────────────────────────────────────────

    @Test
    void authorizeRetake_notPedagogical_throwsIllegalStateException() {
        claim.setType(ClaimType.TECHNICAL);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pedagogical claims");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void authorizeRetake_alreadyResolved_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.RESOLVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed or canceled");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void authorizeRetake_alreadyCanceled_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.CANCELED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed or canceled");

        verify(claimRepository, never()).save(any());
    }
```

- [ ] **Step 2 : Lancer tous les tests unitaires**

```bash
cd backend/claim-service && mvn test -Dtest=ClaimServiceTest -q
```

Résultat attendu : `BUILD SUCCESS`, 9 tests passés.

- [ ] **Step 3 : Commit**

```bash
git add backend/claim-service/src/test/java/tn/english/school/claimservice/service/ClaimServiceTest.java
git commit -m "test(claim): add unit tests for error cases — duplicate, status guards, retake rules"
```

---

## Task 4 : Tests d'intégration

**Files:**
- Create: `backend/claim-service/src/test/java/tn/english/school/claimservice/integration/ClaimServiceIntegrationTest.java`

- [ ] **Step 1 : Créer ClaimServiceIntegrationTest**

Créer `backend/claim-service/src/test/java/tn/english/school/claimservice/integration/ClaimServiceIntegrationTest.java` :

```java
package tn.english.school.claimservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.entity.User;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.exception.DuplicateClaimException;
import tn.english.school.claimservice.repository.UserRepository;
import tn.english.school.claimservice.service.ClaimService;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimServiceIntegrationTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private UserRepository userRepository;

    private User student;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(99L);
        student.setFirstName("Bob");
        student.setLastName("Dupont");
        student.setEmail("bob@esprit.tn");
        userRepository.save(student);
    }

    // ── Persistance basique ───────────────────────────────────────────────────

    @Test
    void createClaim_persistedInDb_withGeneratedId() {
        Claim claim = buildClaim("Math exam grade", ClaimType.PEDAGOGICAL);

        Claim saved = claimService.createClaim(claim);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ClaimStatus.OPEN);
    }

    // ── Anti-doublon persisté ─────────────────────────────────────────────────

    @Test
    void createClaim_duplicate_throwsDuplicateClaimException() {
        Claim first = buildClaim("Physics exam", ClaimType.PEDAGOGICAL);
        claimService.createClaim(first);

        Claim duplicate = buildClaim("Physics exam", ClaimType.PEDAGOGICAL);

        assertThatThrownBy(() -> claimService.createClaim(duplicate))
                .isInstanceOf(DuplicateClaimException.class)
                .hasMessageContaining("Duplicate Alert");
    }

    // ── Cycle complet ─────────────────────────────────────────────────────────

    @Test
    void fullCycle_create_authorize_link() {
        // 1. Créer un claim pédagogique
        Claim claim = buildClaim("Biology retake", ClaimType.PEDAGOGICAL);
        Claim created = claimService.createClaim(claim);
        assertThat(created.getStatus()).isEqualTo(ClaimStatus.OPEN);

        // 2. Autoriser le retake
        Claim authorized = claimService.authorizeRetake(created.getId());
        assertThat(authorized.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);

        // 3. Lier un retake request
        Claim linked = claimService.linkRetakeRequest(authorized.getId(), 42L);
        assertThat(linked.getRetakeRequestId()).isEqualTo(42L);
        assertThat(linked.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Claim buildClaim(String subject, ClaimType type) {
        Claim claim = new Claim();
        claim.setSubject(subject);
        claim.setDescription("Test description");
        claim.setType(type);
        claim.setStudent(student);
        return claim;
    }
}
```

- [ ] **Step 2 : Lancer les tests d'intégration**

```bash
cd backend/claim-service && mvn test -Dtest=ClaimServiceIntegrationTest -q
```

Résultat attendu : `BUILD SUCCESS`, 3 tests passés.

- [ ] **Step 3 : Lancer la suite complète**

```bash
cd backend/claim-service && mvn test -q
```

Résultat attendu : `BUILD SUCCESS`, 12 tests passés au total.

- [ ] **Step 4 : Commit final**

```bash
git add backend/claim-service/src/test/java/tn/english/school/claimservice/integration/ClaimServiceIntegrationTest.java
git commit -m "test(claim): add integration tests — persistence, anti-duplicate, full retake cycle"
```

---

## Résumé de la couverture

| Cas testé | Type | Test |
|-----------|------|------|
| Création réussie | Unitaire | `createClaim_success` |
| Doublon actif → exception | Unitaire | `createClaim_duplicateActive_throwsDuplicateClaimException` |
| Update interdit si statut != OPEN | Unitaire | `updateClaim_statusNotOpen_throwsIllegalStateException` |
| Delete interdit si statut != OPEN | Unitaire | `deleteClaim_statusNotOpen_throwsIllegalStateException` |
| Autorisation retake réussie | Unitaire | `authorizeRetake_success` |
| Idempotence si déjà RETAKE_AUTHORIZED | Unitaire | `authorizeRetake_alreadyAuthorized_returnsExistingWithoutSave` |
| Type != PEDAGOGICAL → exception | Unitaire | `authorizeRetake_notPedagogical_throwsIllegalStateException` |
| Statut RESOLVED → exception | Unitaire | `authorizeRetake_alreadyResolved_throwsIllegalStateException` |
| Statut CANCELED → exception | Unitaire | `authorizeRetake_alreadyCanceled_throwsIllegalStateException` |
| Persistance en base H2 | Intégration | `createClaim_persistedInDb_withGeneratedId` |
| Anti-doublon persisté | Intégration | `createClaim_duplicate_throwsDuplicateClaimException` |
| Cycle complet create→authorize→link | Intégration | `fullCycle_create_authorize_link` |
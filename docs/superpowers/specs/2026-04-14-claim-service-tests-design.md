# Design — Tests claim-service (Sprint 2)

**Date:** 2026-04-14
**Contexte:** Préparation validation Sprint 2. Le prof exige des tests JUnit/Mockito couvrant la logique métier complexe, pas uniquement le CRUD.

---

## Approche choisie

Approche A : Tests unitaires (Mockito) + Tests d'intégration (SpringBootTest + H2 en mémoire).

---

## Structure des fichiers

```
claim-service/src/test/java/tn/english/school/claimservice/
├── service/
│   └── ClaimServiceTest.java              ← tests unitaires (Mockito)
└── integration/
    └── ClaimServiceIntegrationTest.java   ← tests intégration (SpringBootTest + H2)
```

Dépendance à ajouter dans `claim-service/pom.xml` :
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Tests unitaires — `ClaimServiceTest.java`

Annotations : `@ExtendWith(MockitoExtension.class)`. Les repositories sont mockés avec Mockito.

| Test | Méthode | Résultat attendu |
|------|---------|-----------------|
| `createClaim_success` | `createClaim()` | Claim sauvegardé avec statut OPEN |
| `createClaim_duplicateActive_throwsDuplicateClaimException` | `createClaim()` | `DuplicateClaimException` levée |
| `updateClaim_statusNotOpen_throwsIllegalStateException` | `updateClaim()` | `IllegalStateException` si statut != OPEN |
| `deleteClaim_statusNotOpen_throwsIllegalStateException` | `deleteClaim()` | `IllegalStateException` si statut != OPEN |
| `authorizeRetake_success` | `authorizeRetake()` | Statut devient RETAKE_AUTHORIZED |
| `authorizeRetake_notPedagogical_throwsIllegalStateException` | `authorizeRetake()` | `IllegalStateException` si type != PEDAGOGICAL |
| `authorizeRetake_alreadyResolved_throwsIllegalStateException` | `authorizeRetake()` | `IllegalStateException` si RESOLVED ou CANCELED |
| `authorizeRetake_alreadyAuthorized_returnsExisting` | `authorizeRetake()` | Retourne l'existant sans appel à `save()` |

---

## Tests d'intégration — `ClaimServiceIntegrationTest.java`

Annotations : `@SpringBootTest`, `@Transactional` (rollback automatique après chaque test). Base H2 en mémoire configurée via `application-test.properties`.

| Test | Ce qui est vérifié |
|------|--------------------|
| `createClaim_persistedInDb` | ID généré non nul, statut = OPEN en base |
| `createClaim_duplicate_throwsException` | 2ème claim identique (même étudiant, même sujet, statut OPEN) lève `DuplicateClaimException` |
| `fullCycle_createAuthorizeLink` | Claim créé → autorisé (RETAKE_AUTHORIZED) → lié à un retake request ID — vérification de chaque état en base |

---

## Configuration test

Fichier `claim-service/src/test/resources/application-test.properties` :
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## Hors périmètre (pour cette itération)

- Tests de sécurité JWT (endpoints) — décidé hors scope
- Tests retake-service — prochaine itération
- Tests Angular (Karma/Jasmine) — prochaine itération
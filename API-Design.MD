# 🚀 OPOSSUM API - Architecture Monolithique

## 📋 Vue d'ensemble

API REST pour l'application OPOSSUM de signalement d'objets perdus/trouvés, basée sur **UNE SEULE application Spring Boot** - Architecture monolithique pour simplicité et rapidité de développement.

---

## 🏗️ Architecture Simplifiée

### Une seule application Spring Boot (Port: 8080)
```
┌─────────────────────────────────────────┐
│         OPOSSUM API (Spring Boot)       │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │          CONTROLLERS                ││
│  │  • AuthController                   ││
│  │  • UserController                   ││
│  │  • AnnonceController                ││
│  │  • MessageController                ││
│  │  • FileController                   ││
│  │  • AdminController                  ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │           SERVICES                  ││
│  │  • UserService                      ││
│  │  • AnnonceService                   ││
│  │  • MessageService                   ││
│  │  • FileService                      ││
│  │  • EmailService                     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │         REPOSITORIES                ││
│  │  • UserRepository                   ││
│  │  • AnnonceRepository                ││
│  │  • MessageRepository                ││
│  │  • FileRepository                   ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
                    │
                    ▼
        ┌─────────────────────┐
        │  PostgreSQL Database │
        │     opossum_db      │
        └─────────────────────┘
```

**Avantages pour vous :**
- ✅ **Un seul projet** à gérer
- ✅ **Une seule base de données**
- ✅ **Pas de communication réseau** entre services
- ✅ **Débogage plus simple**
- ✅ **Déploiement plus facile**

---

## 🔐 Authentification & Sécurité

### JWT Tokens (comme prévu)
- **Access Token** : 30 minutes
- **Refresh Token** : 7 jours
- Stockage en base de données

### Rôles
- `USER` : utilisateur standard
- `ADMIN` : administrateur/modérateur

---

## 📊 Endpoints API Simplifiés

### 🔑 Authentification - `/api/v1/auth`
```http
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### 👤 Utilisateurs - `/api/v1/users`
```http
GET    /api/v1/users/profile          # Mon profil
PUT    /api/v1/users/profile          # Modifier mon profil
DELETE /api/v1/users/profile          # Supprimer mon compte
GET    /api/v1/users/{id}             # Profil public
```

### 📣 Annonces - `/api/v1/annonces`
```http
GET    /api/v1/annonces               # Liste avec filtres
POST   /api/v1/annonces               # Créer
GET    /api/v1/annonces/{id}          # Détail
PUT    /api/v1/annonces/{id}          # Modifier
DELETE /api/v1/annonces/{id}          # Supprimer
GET    /api/v1/annonces/search        # Recherche
```

### 💬 Messages - `/api/v1/conversations`
```http
GET    /api/v1/conversations          # Mes conversations
POST   /api/v1/conversations          # Nouvelle conversation
GET    /api/v1/conversations/{id}/messages  # Messages
POST   /api/v1/conversations/{id}/messages  # Envoyer message
```

### 📁 Fichiers - `/api/v1/files`
```http
POST   /api/v1/files/upload           # Upload photo
GET    /api/v1/files/{id}             # Télécharger
```

### 📈 Admin - `/api/v1/admin` (Optionnel pour v1)
```http
GET    /api/v1/admin/stats            # Statistiques
GET    /api/v1/admin/users            # Gestion utilisateurs
GET    /api/v1/admin/annonces         # Modération annonces
```

---

## 🗄️ Base de Données Simplifiée

### Une seule base : `opossum_db`

#### Tables principales :
```sql
-- Utilisateurs
users (id, email, password, first_name, last_name, phone, role, created_at)

-- Tokens de refresh
refresh_tokens (id, user_id, token, expires_at)

-- Annonces
annonces (id, title, description, type, category, status, 
          latitude, longitude, address, city, user_id, created_at)

-- Photos des annonces
annonce_photos (id, annonce_id, file_path, is_primary)

-- Conversations
conversations (id, annonce_id, user1_id, user2_id, created_at)

-- Messages
messages (id, conversation_id, sender_id, content, sent_at, is_read)

-- Fichiers uploadés
files (id, original_name, file_path, mime_type, user_id, created_at)
```

**Beaucoup plus simple !** Toutes les données dans une seule base.

---

## 🚀 Plan de développement par étapes

### Phase 1 : Base (1 semaine)
1. **Setup projet Spring Boot**
   - Spring Web, Security, JPA, PostgreSQL
   - Configuration de base

2. **Authentification**
   - Inscription/Connexion
   - JWT tokens
   - Sécurisation des endpoints

3. **CRUD Utilisateurs**
   - Profil utilisateur
   - Gestion de compte

### Phase 2 : Fonctionnalités principales (1 semaine)
1. **CRUD Annonces**
   - Créer/modifier/supprimer annonces
   - Géolocalisation basique

2. **Upload de photos**
   - Stockage local des fichiers
   - Association aux annonces

3. **Recherche simple**
   - Filtrage par type (LOST/FOUND)
   - Recherche par ville

### Phase 3 : Fonctionnalités avancées (1 semaine)
1. **Messagerie basique**
   - Conversations entre utilisateurs
   - CRUD messages

2. **Recherche géographique**
   - Recherche par proximité
   - Calcul de distance

3. **Interface admin basique**
   - Statistiques simples
   - Modération

---

## 🛠️ Technologies recommandées

### Backend
```xml
<!-- pom.xml simplifié -->
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### Structure du projet
```
src/main/java/com/opossum/
├── OpossumApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── JwtConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── AnnonceController.java
│   └── MessageController.java
├── service/
│   ├── UserService.java
│   ├── AnnonceService.java
│   └── MessageService.java
├── repository/
│   ├── UserRepository.java
│   ├── AnnonceRepository.java
│   └── MessageRepository.java
├── entity/
│   ├── User.java
│   ├── Annonce.java
│   └── Message.java
└── dto/
    ├── UserDTO.java
    ├── AnnonceDTO.java
    └── MessageDTO.java
```

---

## 🎯 Avantages de cette approche

### Pour l'apprentissage
- ✅ **Focus sur la logique métier** plutôt que sur l'infrastructure
- ✅ **Concepts Spring Boot classiques**
- ✅ **Debugging facile** - tout dans un seul processus
- ✅ **Tests plus simples**

### Pour le développement
- ✅ **Développement plus rapide**
- ✅ **Moins de configuration**
- ✅ **Déploiement simple** (un seul JAR)
- ✅ **Base solide** pour évoluer vers les microservices plus tard

### Pour le projet
- ✅ **Respecte le brief** (Spring Boot + PostgreSQL + JWT)
- ✅ **Toutes les fonctionnalités** demandées
- ✅ **Temps de développement réaliste**
- ✅ **Moins de risques techniques**

---

## 🔄 Migration future possible

Si plus tard vous voulez passer aux microservices, cette architecture monolithique sera une excellente base :

1. **User Service** = Package `user` + table `users`
2. **Annonce Service** = Package `annonce` + tables `annonces` + `annonce_photos`
3. **Message Service** = Package `message` + tables `conversations` + `messages`

**Conclusion :** Commencez simple, apprenez bien les bases, et évoluez progressivement ! 🎓

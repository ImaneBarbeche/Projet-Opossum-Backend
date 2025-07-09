# 📚 OPOSSUM API - Documentation Wiki

## 🏠 Page d'accueil API

Bienvenue dans la documentation de l'API OPOSSUM - Application de signalement d'objets perdus/trouvés

---

## 👤 Authentification

### 🔐 Inscription
**Dernière modification par [Votre nom] Il y a X jours**

**👤 Register New User**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/register

📖 **Description**
Permet à un nouvel utilisateur de créer un compte sur la plateforme OPOSSUM. Un email de vérification sera envoyé après l'inscription.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Body Parameters:**
```json
{
    "email": "string (required)",
    "password": "string (required, min 8 chars)",
    "firstName": "string (required)",
    "lastName": "string (required)",
    "phone": "string (optional)"
}
```

**Example:**
```json
POST /api/v1/auth/register
Content-Type: application/json

{
    "email": "marie.dupont@example.com",
    "password": "motdepasse123",
    "firstName": "Marie",
    "lastName": "Dupont",
    "phone": "+33123456789"
}
```

✅ **Validation Rules**
- 📧 Email valide et unique requis
- 🔒 Mot de passe minimum 8 caractères
- 👤 Prénom et nom requis (2-50 caractères)
- 📱 Téléphone optionnel (format français)

📤 **Response**

✅ **Success (201)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "email": "marie.dupont@example.com",
        "firstName": "Marie",
        "lastName": "Dupont",
        "isEmailVerified": false
    },
    "message": "Compte créé avec succès. Vérifiez votre email.",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Validation Error (400)**
```json
{
    "success": false,
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Données invalides",
        "details": {
            "email": "Email déjà utilisé",
            "password": "Mot de passe trop court"
        }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

🔴 **Server Error (500)**
```json
{
    "success": false,
    "error": {
        "code": "INTERNAL_ERROR",
        "message": "Erreur lors de la création du compte: [error details]"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 📧 Un email de vérification est envoyé automatiquement
- 🔒 Le mot de passe est hashé avec bcrypt
- 👤 Role USER assigné par défaut
- ⏰ Token de vérification expire en 24h

---

### 🔐 Connexion
**Dernière modification par [Votre nom] Il y a X jours**

**🔑 User Login**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/login

📖 **Description**
Authentifie un utilisateur et retourne les tokens JWT (access + refresh) pour accéder aux endpoints protégés.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Body Parameters:**
```json
{
    "email": "string (required)",
    "password": "string (required)"
}
```

**Example:**
```json
POST /api/v1/auth/login
Content-Type: application/json

{
    "email": "marie.dupont@example.com",
    "password": "motdepasse123"
}
```

✅ **Validation Rules**
- 📧 Email valide requis
- 🔒 Mot de passe requis
- ✅ Compte doit être vérifié
- ❌ Compte ne doit pas être bloqué

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "user": {
            "id": "550e8400-e29b-41d4-a716-446655440001",
            "email": "marie.dupont@example.com",
            "firstName": "Marie",
            "lastName": "Dupont",
            "role": "USER"
        },
        "tokens": {
            "accessToken": "eyJhbGciOiJIUzI1NiIs...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
            "expiresIn": 1800
        }
    },
    "message": "Connexion réussie",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Authentication Error (401)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_CREDENTIALS",
        "message": "Email ou mot de passe incorrect"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Account Blocked (403)**
```json
{
    "success": false,
    "error": {
        "code": "ACCOUNT_BLOCKED",
        "message": "Votre compte a été suspendu"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 🔑 Access token expire en 30 minutes
- 🔄 Refresh token expire en 7 jours
- 📱 Stockez les tokens de manière sécurisée côté client
- ⏰ lastLoginAt mis à jour automatiquement

---

### 🔄 Refresh Token
**Dernière modification par [Votre nom] Il y a X jours**

**🔄 Refresh Access Token**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/refresh

📖 **Description**
Génère un nouveau token d'accès à partir d'un refresh token valide.

🔐 **Authentication**
Refresh Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "refreshToken": "string (required)"
}
```

**Example:**
```json
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 1800
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Invalid Token (401)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_REFRESH_TOKEN",
        "message": "Refresh token invalide ou expiré"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

---

### 🚪 Déconnexion
**Dernière modification par [Votre nom] Il y a X jours**

**🚪 User Logout**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/logout

📖 **Description**
Déconnecte l'utilisateur et révoque ses tokens d'accès et de refresh.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "refreshToken": "string (optional)"
}
```

**Example:**
```json
POST /api/v1/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Déconnexion réussie",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

---

### 🔑 Mot de Passe Oublié
**Dernière modification par [Votre nom] Il y a X jours**

**🔑 Forgot Password**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/forgot-password

📖 **Description**
Envoie un email avec un lien de réinitialisation de mot de passe.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Body Parameters:**
```json
{
    "email": "string (required)"
}
```

**Example:**
```json
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
    "email": "marie.dupont@example.com"
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Email de réinitialisation envoyé",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

---

### 🔐 Réinitialiser Mot de Passe
**Dernière modification par [Votre nom] Il y a X jours**

**🔐 Reset Password**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/auth/reset-password

📖 **Description**
Réinitialise le mot de passe avec un token reçu par email.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Body Parameters:**
```json
{
    "token": "string (required)",
    "newPassword": "string (required, min 8 chars)"
}
```

**Example:**
```json
POST /api/v1/auth/reset-password
Content-Type: application/json

{
    "token": "reset-token-abc123",
    "newPassword": "nouveaumotdepasse123"
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Mot de passe réinitialisé avec succès",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Invalid Token (400)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_TOKEN",
        "message": "Token invalide ou expiré"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 🔑 Token valide pendant 1 heure seulement
- 🔒 Invalidation automatique après utilisation
- 🔄 Nouvelle connexion requise après reset

---

### ✅ Vérification d'Email
**Dernière modification par [Votre nom] Il y a X jours**

**✅ Verify Email**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/auth/verify-email

📖 **Description**
Vérifie l'email d'un utilisateur avec le token reçu par email après inscription.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Query Parameters:**
- `token` (string, required): Token de vérification reçu par email

**Example:**
```
GET /api/v1/auth/verify-email?token=verify-token-abc123
```

✅ **Validation Rules**
- 🔑 Token valide et non expiré requis
- 📧 Email pas déjà vérifié

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Email vérifié avec succès. Vous pouvez maintenant vous connecter.",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Invalid Token (400)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_VERIFICATION_TOKEN",
        "message": "Token de vérification invalide ou expiré"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Already Verified (409)**
```json
{
    "success": false,
    "error": {
        "code": "EMAIL_ALREADY_VERIFIED",
        "message": "Cet email est déjà vérifié"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 🔑 Token expire en 24h après inscription
- ✅ Marque automatiquement le compte comme vérifié
- 📧 Redirection possible vers l'app mobile après vérification

---

## 👤 Gestion des Utilisateurs

### 👤 Profil Utilisateur Actuel
**Dernière modification par [Votre nom] Il y a X jours**

**👤 Get Current User Profile**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/users/profile

📖 **Description**
Récupère les informations complètes du profil de l'utilisateur actuellement connecté. Cet endpoint permet aux utilisateurs de consulter leurs propres détails de compte.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Query Parameters:**
Aucun paramètre requis ❌

**Example:**
```http
GET /api/v1/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 👤 Compte utilisateur valide requis
- ✅ Compte doit être vérifié et non bloqué

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "email": "marie.dupont@example.com",
        "firstName": "Marie",
        "lastName": "Dupont",
        "phone": "+33123456789",
        "avatar": "/files/avatar-123.jpg",
        "role": "USER",
        "isActive": true,
        "isEmailVerified": true,
        "createdAt": "2025-01-15T10:00:00Z",
        "lastLoginAt": "2025-07-09T08:30:00Z"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Authentication Error (401)**
```json
{
    "success": false,
    "error": {
        "code": "UNAUTHORIZED",
        "message": "Token JWT invalide ou expiré"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

🔴 **Server Error (500)**
```json
{
    "success": false,
    "error": {
        "code": "INTERNAL_ERROR",
        "message": "Erreur lors de la récupération du profil utilisateur: [error details]"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 👤 Retourne uniquement les informations du profil de l'utilisateur actuel
- 🔒 Inclut les données privées (email, téléphone, statuts)
- ❌ Le mot de passe hashé n'est jamais retourné
- 🎯 Parfait pour les pages de profil et tableaux de bord utilisateur

---

### 👥 Profil Public d'un Utilisateur
**Dernière modification par [Votre nom] Il y a X jours**

**👥 Get Public User Profile**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/users/{id}

📖 **Description**
Récupère les informations publiques d'un utilisateur spécifique. Utilisé pour afficher le profil du propriétaire d'une annonce.

🔐 **Authentication**
JWT Token optionnel 🔓

📥 **Input**
**Path Parameters:**
- `id` (UUID, required): Identifiant unique de l'utilisateur

**Example:**
```http
GET /api/v1/users/550e8400-e29b-41d4-a716-446655440001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...  # Optionnel
```

✅ **Validation Rules**
- 🆔 ID utilisateur valide (format UUID)
- 👤 Utilisateur doit exister et être actif
- 🚫 Utilisateur ne doit pas être bloqué

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "firstName": "Marie",
        "lastName": "Dupont",
        "avatar": "/files/avatar-456.jpg",
        "createdAt": "2024-12-15T14:20:00Z"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

❌ **Not Found (404)**
```json
{
    "success": false,
    "error": {
        "code": "USER_NOT_FOUND",
        "message": "Utilisateur introuvable"
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 🔓 Accessible sans authentification pour certains cas
- 🙈 Données privées (email, téléphone) non exposées
- 👀 Idéal pour voir qui a posté une annonce
- 🛡️ Respecte la vie privée des utilisateurs

---

### ✏️ Modifier le Profil Utilisateur
**Dernière modification par [Votre nom] Il y a X jours**

**✏️ Update User Profile**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/users/profile

📖 **Description**
Met à jour les informations du profil de l'utilisateur connecté (nom, téléphone, avatar).

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "firstName": "string (optional)",
    "lastName": "string (optional)",
    "phone": "string (optional)",
    "avatar": "string (optional, file path)"
}
```

**Example:**
```json
PUT /api/v1/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "firstName": "Marie-Claire",
    "lastName": "Dupont-Martin",
    "phone": "+33123456790"
}
```

✅ **Validation Rules**
- 👤 Prénom optionnel (2-50 caractères)
- 👤 Nom optionnel (2-50 caractères)
- 📱 Téléphone optionnel (format français)
- 🖼️ Avatar optionnel (URL de fichier uploadé)

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "email": "marie.dupont@example.com",
        "firstName": "Marie-Claire",
        "lastName": "Dupont-Martin",
        "phone": "+33123456790",
        "avatar": "/files/avatar-456.jpg",
        "createdAt": "2024-12-15T14:20:00Z",
        "updatedAt": "2025-07-09T21:00:00Z"
    },
    "message": "Profil mis à jour avec succès",
    "timestamp": "2025-07-09T21:00:00Z"
}
```

❌ **Validation Error (400)**
```json
{
    "success": false,
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Données invalides",
        "details": {
            "phone": "Format de téléphone invalide"
        }
    },
    "timestamp": "2025-07-09T21:00:00Z"
}
```

📝 **Notes**
- ✏️ Modification partielle autorisée (champs optionnels)
- 📧 Email non modifiable via cet endpoint
- 🔄 Champ `updatedAt` automatiquement mis à jour
- 🖼️ Avatar uploadé séparément via endpoint files

---

### 🔐 Changer le Mot de Passe
**Dernière modification par [Votre nom] Il y a X jours**

**🔐 Change Password**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/users/password

📖 **Description**
Permet à l'utilisateur de changer son mot de passe en fournissant l'ancien.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "currentPassword": "string (required)",
    "newPassword": "string (required)"
}
```

**Example:**
```json
PUT /api/v1/users/password
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "currentPassword": "ancienMotDePasse123",
    "newPassword": "nouveauMotDePasse456!"
}
```

✅ **Validation Rules**
- 🔒 Mot de passe actuel requis et valide
- 🔒 Nouveau mot de passe requis (min 8 caractères)
- 🚫 Nouveau mot de passe différent de l'actuel

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Mot de passe modifié avec succès",
    "timestamp": "2025-07-09T21:15:00Z"
}
```

❌ **Unauthorized (401)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_CURRENT_PASSWORD",
        "message": "Mot de passe actuel incorrect"
    },
    "timestamp": "2025-07-09T21:15:00Z"
}
```

📝 **Notes**
- 🔐 Vérification obligatoire du mot de passe actuel
- 🔄 Invalidation automatique des tokens existants
- 📧 Email de notification automatique
- 🛡️ Nouvelle connexion requise après changement

---

### 🗑️ Supprimer son Compte
**Dernière modification par [Votre nom] Il y a X jours**

**🗑️ Delete User Account**

🔗 **Endpoint**
- Method: DELETE 🔴
- URL: http://localhost:8080/api/v1/users/account

📖 **Description**
Permet à l'utilisateur de supprimer définitivement son propre compte et toutes ses données.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "password": "string (required)",
    "confirmDeletion": "boolean (required, must be true)"
}
```

**Example:**
```json
DELETE /api/v1/users/account
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "password": "motdepasse123",
    "confirmDeletion": true
}
```

✅ **Validation Rules**
- 🔒 Mot de passe actuel requis et valide
- ✅ Confirmation explicite requise (confirmDeletion = true)
- 👤 Utilisateur doit être actif

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Compte supprimé définitivement",
    "timestamp": "2025-07-09T21:30:00Z"
}
```

❌ **Unauthorized (401)**
```json
{
    "success": false,
    "error": {
        "code": "INVALID_PASSWORD",
        "message": "Mot de passe incorrect"
    },
    "timestamp": "2025-07-09T21:30:00Z"
}
```

❌ **Validation Error (400)**
```json
{
    "success": false,
    "error": {
        "code": "CONFIRMATION_REQUIRED",
        "message": "Confirmation de suppression requise"
    },
    "timestamp": "2025-07-09T21:30:00Z"
}
```

📝 **Notes**
- ⚠️ Suppression définitive et irréversible
- 🗂️ Toutes données supprimées (annonces, messages, fichiers)
- 🔄 Tokens invalidés immédiatement
- 📧 Email de confirmation envoyé

---

### 📄 Mes Annonces
**Dernière modification par [Votre nom] Il y a X jours**

**📄 Get My Announcements**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/users/my-annonces

📖 **Description**
Récupère toutes les annonces de l'utilisateur connecté avec possibilité de filtrage.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Query Parameters:**
- `type` (string, optional): LOST ou FOUND
- `status` (string, optional): ACTIVE, RESOLVED, EXPIRED
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 20)
- `sort` (string, optional): createdAt,desc|createdAt,asc

**Example:**
```
GET /api/v1/users/my-annonces?type=FOUND&status=ACTIVE&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 👤 Utilisateur actif requis

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440002",
                "title": "iPhone 13 Pro trouvé",
                "description": "iPhone trouvé près de la gare",
                "type": "FOUND",
                "category": "electronics",
                "status": "ACTIVE",
                "location": {
                    "latitude": 48.8566,
                    "longitude": 2.3522,
                    "address": "Gare du Nord, Paris"
                },
                "createdAt": "2025-07-09T08:30:00Z",
                "updatedAt": "2025-07-09T08:30:00Z",
                "thumbnailUrl": "/files/thumb_img_001.jpg",
                "messageCount": 3,
                "viewCount": 25
            }
        ],
        "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 👤 Affiche uniquement les annonces de l'utilisateur connecté
- 📊 Inclut statistiques (messages, vues) pour gestion
- 🔄 Tri par date de création par défaut
- ✏️ Accès direct pour modification/suppression

---

## 📣 Gestion des Annonces

### 📋 Liste des Annonces
**Dernière modification par [Votre nom] Il y a X jours**

**📋 Get Announcements List**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/annonces

📖 **Description**
Récupère la liste des annonces actives avec possibilité de filtrage. Endpoint principal pour l'affichage de la page d'accueil et la recherche.

🔐 **Authentication**
JWT Token optionnel 🔓

📥 **Input**
**Query Parameters:**
```
type: string (optional) - LOST|FOUND
category: string (optional) - electronics|clothing|documents|etc.
city: string (optional) - Ville de recherche
latitude: number (optional) - Latitude pour recherche géographique
longitude: number (optional) - Longitude pour recherche géographique
radius: number (optional) - Rayon en km (défaut: 10)
dateFrom: date (optional) - Format: yyyy-mm-dd
dateTo: date (optional) - Format: yyyy-mm-dd
page: number (optional) - Page (défaut: 0)
size: number (optional) - Taille page (défaut: 20, max: 100)
sort: string (optional) - createdAt,desc|createdAt,asc
```

**Examples:**
```http
# Toutes les annonces
GET /api/v1/annonces

# Objets perdus à Paris
GET /api/v1/annonces?type=LOST&city=Paris

# Électronique dans un rayon de 5km
GET /api/v1/annonces?category=electronics&latitude=48.8566&longitude=2.3522&radius=5

# Pagination
GET /api/v1/annonces?page=1&size=10&sort=createdAt,desc
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "title": "iPhone 14 perdu",
                "description": "iPhone 14 Pro bleu perdu près de la gare",
                "type": "LOST",
                "category": "electronics",
                "status": "ACTIVE",
                "location": {
                    "latitude": 48.8566,
                    "longitude": 2.3522,
                    "address": "Gare du Nord",
                    "city": "Paris"
                },
                "photos": ["/files/photo1.jpg"],
                "userFirstName": "Jean",
                "distance": 2.3,
                "createdAt": "2025-07-09T10:30:00Z"
            }
        ],
        "page": {
            "number": 0,
            "size": 20,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 📍 Si latitude/longitude fournis, inclut la distance calculée
- 🔍 Filtres combinables pour recherche précise
- 📄 Pagination automatique pour performance
- 🎯 Optimisé pour l'affichage mobile et web

---

### ➕ Créer une Annonce
**Dernière modification par [Votre nom] Il y a X jours**

**➕ Create New Announcement**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/annonces

📖 **Description**
Permet à un utilisateur authentifié de créer une nouvelle annonce d'objet perdu ou trouvé avec photos et géolocalisation.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "title": "string (required, 5-200 chars)",
    "description": "string (required, 10-2000 chars)",
    "type": "string (required, LOST|FOUND)",
    "category": "string (required)",
    "location": {
        "latitude": "number (required, -90 to 90)",
        "longitude": "number (required, -180 to 180)",
        "address": "string (optional)",
        "city": "string (required)"
    },
    "contactInfo": {
        "phone": "string (optional)",
        "email": "string (optional)"
    },
    "photos": ["string (array of file IDs, max 5)"]
}
```

**Example:**
```json
POST /api/v1/annonces
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "title": "Portefeuille trouvé",
    "description": "Portefeuille en cuir noir trouvé dans le parc des Buttes-Chaumont",
    "type": "FOUND",
    "category": "documents",
    "location": {
        "latitude": 48.8799,
        "longitude": 2.3832,
        "address": "Parc des Buttes-Chaumont",
        "city": "Paris"
    },
    "contactInfo": {
        "phone": "+33123456789"
    },
    "photos": ["file-id-1", "file-id-2"]
}
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 📝 Titre et description requis (longueurs respectées)
- 📍 Coordonnées GPS valides requises
- 🏙️ Ville requise
- 📷 Maximum 5 photos par annonce
- 📱 Téléphone optionnel (format français si fourni)

📤 **Response**

✅ **Success (201)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440002",
        "title": "Portefeuille trouvé",
        "type": "FOUND",
        "status": "ACTIVE",
        "location": {
            "city": "Paris"
        },
        "createdAt": "2025-07-09T14:25:00Z"
    },
    "message": "Annonce créée avec succès",
    "timestamp": "2025-07-09T14:25:00Z"
}
```

❌ **Validation Error (400)**
```json
{
    "success": false,
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Données invalides",
        "details": {
            "title": "Le titre doit contenir entre 5 et 200 caractères",
            "location.latitude": "Latitude invalide"
        }
    },
    "timestamp": "2025-07-09T14:25:00Z"
}
```

📝 **Notes**
- 📷 Photos doivent être uploadées avant via `/api/v1/files/upload`
- 🆔 L'utilisateur est automatiquement défini comme propriétaire
- ✅ Statut ACTIVE assigné automatiquement
- 📧 Notifications possibles aux utilisateurs intéressés

---

### 📄 Détail d'une Annonce
**Dernière modification par [Votre nom] Il y a X jours**

**📄 Get Announcement Details**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/annonces/{id}

📖 **Description**
Récupère les détails complets d'une annonce spécifique, incluant toutes les photos et informations de contact.

🔐 **Authentication**
JWT Token optionnel 🔓

📥 **Input**
**Path Parameters:**
- `id` (UUID, required): Identifiant unique de l'annonce

**Example:**
```http
GET /api/v1/annonces/550e8400-e29b-41d4-a716-446655440002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...  # Optionnel
```

✅ **Validation Rules**
- 🆔 ID annonce valide (format UUID)
- 📄 Annonce doit exister
- ✅ Annonce doit être active (ou appartenir à l'utilisateur connecté)

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440002",
        "title": "Portefeuille trouvé",
        "description": "Portefeuille en cuir noir trouvé dans le parc des Buttes-Chaumont. Contient des papiers d'identité.",
        "type": "FOUND",
        "category": "documents",
        "status": "ACTIVE",
        "location": {
            "latitude": 48.8799,
            "longitude": 2.3832,
            "address": "Parc des Buttes-Chaumont",
            "city": "Paris"
        },
        "photos": [
            "/files/photo1.jpg",
            "/files/photo2.jpg"
        ],
        "contactInfo": {
            "phone": "+33123456789",
            "email": "marie@example.com"
        },
        "user": {
            "id": "user-marie-id",
            "firstName": "Marie",
            "lastName": "Dupont",
            "avatar": "/files/avatar-marie.jpg"
        },
        "createdAt": "2025-07-09T14:25:00Z",
        "updatedAt": "2025-07-09T14:25:00Z"
    },
    "timestamp": "2025-07-09T15:30:00Z"
}
```

❌ **Not Found (404)**
```json
{
    "success": false,
    "error": {
        "code": "ANNOUNCEMENT_NOT_FOUND",
        "message": "Annonce introuvable"
    },
    "timestamp": "2025-07-09T15:30:00Z"
}
```

📝 **Notes**
- 👁️ Incrémente automatiquement le compteur de vues
- 📞 Informations de contact visibles pour contact direct
- 👤 Inclut les informations publiques du propriétaire
- 💬 Bouton "Contacter" peut initier une conversation

---

### ✏️ Modifier une Annonce
**Dernière modification par [Votre nom] Il y a X jours**

**✏️ Update Announcement**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/annonces/{id}

📖 **Description**
Met à jour une annonce existante. Seul l'auteur de l'annonce peut la modifier.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Path Parameters:**
- `id` (UUID, required): Identifiant unique de l'annonce

**Body Parameters:**
```json
{
    "title": "string (optional)",
    "description": "string (optional)",
    "category": "string (optional)",
    "status": "string (optional)"
}
```

**Example:**
```json
PUT /api/v1/annonces/550e8400-e29b-41d4-a716-446655440002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "title": "iPhone 13 Pro trouvé - RÉSOLU",
    "description": "iPhone trouvé près de la gare - Propriétaire retrouvé !",
    "status": "RESOLVED"
}
```

✅ **Validation Rules**
- 📝 Titre optionnel (3-100 caractères)
- 📝 Description optionnelle (10-1000 caractères)
- 📂 Catégorie parmi : electronics, clothing, accessories, documents, keys, other
- 🔄 Statut parmi : ACTIVE, RESOLVED, EXPIRED

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440002",
        "title": "iPhone 13 Pro trouvé - RÉSOLU",
        "description": "iPhone trouvé près de la gare - Propriétaire retrouvé !",
        "type": "FOUND",
        "category": "electronics",
        "status": "RESOLVED",
        "location": {
            "latitude": 48.8566,
            "longitude": 2.3522,
            "address": "Gare du Nord, Paris"
        },
        "createdAt": "2025-07-09T08:30:00Z",
        "updatedAt": "2025-07-09T11:45:00Z"
    },
    "message": "Annonce modifiée avec succès",
    "timestamp": "2025-07-09T11:45:00Z"
}
```

❌ **Forbidden (403)**
```json
{
    "success": false,
    "error": {
        "code": "ACCESS_DENIED",
        "message": "Vous ne pouvez modifier que vos propres annonces"
    },
    "timestamp": "2025-07-09T11:45:00Z"
}
```

📝 **Notes**
- 🔒 Seul l'auteur peut modifier son annonce
- 🔄 Champ `updatedAt` automatiquement mis à jour
- 🚫 Type et localisation non modifiables après création

---

### 🗑️ Supprimer une Annonce
**Dernière modification par [Votre nom] Il y a X jours**

**🗑️ Delete Announcement**

🔗 **Endpoint**
- Method: DELETE 🔴
- URL: http://localhost:8080/api/v1/annonces/{id}

📖 **Description**
Supprime définitivement une annonce. Seul l'auteur ou un administrateur peut supprimer une annonce.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Path Parameters:**
- `id` (UUID, required): Identifiant unique de l'annonce

**Example:**
```
DELETE /api/v1/annonces/550e8400-e29b-41d4-a716-446655440002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Annonce supprimée avec succès",
    "timestamp": "2025-07-09T12:00:00Z"
}
```

❌ **Forbidden (403)**
```json
{
    "success": false,
    "error": {
        "code": "ACCESS_DENIED",
        "message": "Vous ne pouvez supprimer que vos propres annonces"
    },
    "timestamp": "2025-07-09T12:00:00Z"
}
```

📝 **Notes**
- 🔒 Seul l'auteur ou un admin peut supprimer
- ⚠️ Suppression définitive, non réversible
- 🗂️ Messages liés également supprimés

---

### 🔍 Recherche d'Annonces
**Dernière modification par [Votre nom] Il y a X jours**

**🔍 Search Announcements**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/annonces/search

📖 **Description**
Recherche avancée d'annonces avec filtres multiples : mots-clés, localisation, catégorie, date.

🔐 **Authentication**
JWT Token optionnel 🔓

📥 **Input**
**Query Parameters:**
- `q` (string, optional): Mots-clés de recherche
- `type` (string, optional): LOST ou FOUND
- `category` (string, optional): Catégorie d'objet
- `latitude` (number, optional): Latitude pour recherche géographique
- `longitude` (number, optional): Longitude pour recherche géographique
- `radius` (number, optional): Rayon en km (défaut: 10km)
- `dateFrom` (string, optional): Date minimum (ISO 8601)
- `dateTo` (string, optional): Date maximum (ISO 8601)
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 20, max: 100)

**Example:**
```
GET /api/v1/annonces/search?q=iPhone&type=FOUND&category=electronics&latitude=48.8566&longitude=2.3522&radius=5&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...  # Optionnel
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440002",
                "title": "iPhone 13 Pro trouvé",
                "description": "iPhone trouvé près de la gare",
                "type": "FOUND",
                "category": "electronics",
                "status": "ACTIVE",
                "location": {
                    "latitude": 48.8566,
                    "longitude": 2.3522,
                    "address": "Gare du Nord, Paris",
                    "distance": 2.3
                },
                "createdAt": "2025-07-09T08:30:00Z",
                "user": {
                    "firstName": "Marie",
                    "lastName": "D."
                },
                "thumbnailUrl": "/files/thumb_img_001.jpg"
            }
        ],
        "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 🔍 Recherche textuelle sur titre et description
- 📍 Distance calculée si géolocalisation fournie
- 🏷️ Tri par pertinence puis date
- 📄 Pagination standard

---

### 👤 Annonces d'un Utilisateur
**Dernière modification par [Votre nom] Il y a X jours**

**👤 Get User's Announcements**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/users/{userId}/annonces

📖 **Description**
Récupère toutes les annonces d'un utilisateur spécifique.

🔐 **Authentication**
JWT Token optionnel 🔓

📥 **Input**
**Path Parameters:**
- `userId` (UUID, required): Identifiant de l'utilisateur

**Query Parameters:**
- `type` (string, optional): LOST ou FOUND
- `status` (string, optional): ACTIVE, RESOLVED, EXPIRED
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 20)

**Example:**
```
GET /api/v1/users/550e8400-e29b-41d4-a716-446655440001/annonces?type=FOUND&status=ACTIVE
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...  # Optionnel
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440002",
                "title": "iPhone 13 Pro trouvé",
                "type": "FOUND",
                "category": "electronics",
                "status": "ACTIVE",
                "createdAt": "2025-07-09T08:30:00Z",
                "thumbnailUrl": "/files/thumb_img_001.jpg"
            }
        ],
        "page": {
            "number": 0,
            "size": 20,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

📝 **Notes**
- 👁️ Visible pour tous les utilisateurs
- 🏷️ Tri par date de création décroissante
- 📱 Format optimisé pour liste mobile

---

## 💬 Messagerie

### 💬 Liste des Conversations
**Dernière modification par [Votre nom] Il y a X jours**

**💬 Get User Conversations**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/conversations

📖 **Description**
Récupère la liste des conversations de l'utilisateur connecté avec informations du dernier message et statut de lecture.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Query Parameters:**
```
page: number (optional) - Page (défaut: 0)
size: number (optional) - Taille page (défaut: 20)
```

**Example:**
```http
GET /api/v1/conversations
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 👤 Utilisateur actif requis

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "conv-uuid-123",
                "annonce": {
                    "id": "annonce-uuid-456",
                    "title": "iPhone 14 perdu",
                    "type": "LOST"
                },
                "otherParticipant": {
                    "id": "user-uuid-789",
                    "firstName": "Jean",
                    "lastName": "Martin",
                    "avatar": "/files/avatar-jean.jpg"
                },
                "lastMessage": {
                    "content": "Bonjour, j'ai peut-être trouvé votre téléphone",
                    "sentAt": "2025-07-09T16:45:00Z",
                    "senderId": "user-uuid-789"
                },
                "unreadCount": 2,
                "updatedAt": "2025-07-09T16:45:00Z"
            }
        ],
        "page": {
            "number": 0,
            "size": 20,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T17:00:00Z"
}
```

📝 **Notes**
- 📬 Triées par dernière activité (plus récente en premier)
- 🔴 unreadCount indique les messages non lus
- 👥 otherParticipant = l'autre personne dans la conversation
- 📄 Inclut les infos de l'annonce liée pour contexte

---

### 💬 Créer une Conversation
**Dernière modification par [Votre nom] Il y a X jours**

**💬 Start Conversation**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/conversations

📖 **Description**
Démarre une nouvelle conversation autour d'une annonce avec un autre utilisateur.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Body Parameters:**
```json
{
    "annonceId": "string (required, UUID)",
    "recipientId": "string (required, UUID)",
    "initialMessage": "string (required)"
}
```

**Example:**
```json
POST /api/v1/conversations
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "annonceId": "550e8400-e29b-41d4-a716-446655440002",
    "recipientId": "550e8400-e29b-41d4-a716-446655440001",
    "initialMessage": "Bonjour, j'ai peut-être trouvé votre téléphone. Pouvez-vous me décrire sa coque ?"
}
```

✅ **Validation Rules**
- 📄 Annonce doit exister et être active
- 👤 Destinataire doit exister et être actif
- 💬 Message initial requis (5-500 caractères)
- 🚫 Ne peut pas se contacter soi-même

📤 **Response**

✅ **Success (201)**
```json
{
    "success": true,
    "data": {
        "id": "conv-uuid-new-123",
        "annonceId": "550e8400-e29b-41d4-a716-446655440002",
        "participants": [
            {
                "id": "current-user-id",
                "firstName": "Jean",
                "lastName": "Martin"
            },
            {
                "id": "550e8400-e29b-41d4-a716-446655440001",
                "firstName": "Marie",
                "lastName": "Dupont"
            }
        ],
        "createdAt": "2025-07-09T18:00:00Z"
    },
    "message": "Conversation créée avec succès",
    "timestamp": "2025-07-09T18:00:00Z"
}
```

❌ **Conflict (409)**
```json
{
    "success": false,
    "error": {
        "code": "CONVERSATION_EXISTS",
        "message": "Une conversation existe déjà pour cette annonce entre ces utilisateurs"
    },
    "timestamp": "2025-07-09T18:00:00Z"
}
```

📝 **Notes**
- 🔄 Si conversation existe déjà, retourne l'ID existant
- 📄 Liée à une annonce spécifique
- 💬 Message initial automatiquement envoyé

---

### 💬 Messages d'une Conversation
**Dernière modification par [Votre nom] Il y a X jours**

**💬 Get Conversation Messages**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/conversations/{conversationId}/messages

📖 **Description**
Récupère l'historique des messages d'une conversation spécifique.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Path Parameters:**
- `conversationId` (UUID, required): Identifiant de la conversation

**Query Parameters:**
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 50)

**Example:**
```
GET /api/v1/conversations/conv-uuid-123/messages?page=0&size=50
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "msg-uuid-001",
                "content": "Bonjour, j'ai peut-être trouvé votre téléphone",
                "senderId": "user-uuid-789",
                "senderName": "Jean Martin",
                "sentAt": "2025-07-09T16:45:00Z",
                "isRead": true
            },
            {
                "id": "msg-uuid-002", 
                "content": "Merci ! Pouvez-vous me dire de quelle couleur est la coque ?",
                "senderId": "current-user-id",
                "senderName": "Marie Dupont",
                "sentAt": "2025-07-09T16:47:00Z",
                "isRead": false
            }
        ],
        "page": {
            "number": 0,
            "size": 50,
            "totalElements": 2,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T18:30:00Z"
}
```

📝 **Notes**
- 📅 Messages triés du plus récent au plus ancien
- 👁️ Marque automatiquement les messages comme lus
- 🔒 Seuls les participants peuvent voir les messages

---

### 💬 Envoyer un Message
**Dernière modification par [Votre nom] Il y a X jours**

**💬 Send Message**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/conversations/{conversationId}/messages

📖 **Description**
Envoie un nouveau message dans une conversation existante.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Path Parameters:**
- `conversationId` (UUID, required): Identifiant de la conversation

**Body Parameters:**
```json
{
    "content": "string (required)"
}
```

**Example:**
```json
POST /api/v1/conversations/conv-uuid-123/messages
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "content": "Parfait ! Rendez-vous demain à 14h devant la gare. Merci beaucoup !"
}
```

✅ **Validation Rules**
- 💬 Contenu requis (1-1000 caractères)
- 🔒 Utilisateur doit être participant de la conversation
- ✅ Conversation doit être active

📤 **Response**

✅ **Success (201)**
```json
{
    "success": true,
    "data": {
        "id": "msg-uuid-003",
        "content": "Parfait ! Rendez-vous demain à 14h devant la gare. Merci beaucoup !",
        "senderId": "current-user-id",
        "senderName": "Marie Dupont",
        "sentAt": "2025-07-09T18:45:00Z",
        "isRead": false
    },
    "message": "Message envoyé avec succès",
    "timestamp": "2025-07-09T18:45:00Z"
}
```

❌ **Forbidden (403)**
```json
{
    "success": false,
    "error": {
        "code": "ACCESS_DENIED",
        "message": "Vous n'êtes pas participant de cette conversation"
    },
    "timestamp": "2025-07-09T18:45:00Z"
}
```

📝 **Notes**
- 🔄 Notifications push automatiques vers l'autre participant
- 📱 Message apparaît instantanément dans la conversation
- 🕐 Horodatage précis pour synchronisation

---

### 💬 Marquer comme Lu
**Dernière modification par [Votre nom] Il y a X jours**

**💬 Mark Messages as Read**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/conversations/{conversationId}/read

📖 **Description**
Marque tous les messages non lus d'une conversation comme lus.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Path Parameters:**
- `conversationId` (UUID, required): Identifiant de la conversation

**Example:**
```
PUT /api/v1/conversations/conv-uuid-123/read
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "markedAsRead": 3,
        "conversationId": "conv-uuid-123"
    },
    "message": "Messages marqués comme lus",
    "timestamp": "2025-07-09T19:00:00Z"
}
```

📝 **Notes**
- 🔴 Met à jour le compteur `unreadCount` dans la liste des conversations
- 📱 Optimisation pour éviter de marquer message par message
- 👁️ Action automatique à l'ouverture d'une conversation

---

## 📁 Gestion des Fichiers

### 📤 Upload de Fichier
**Dernière modification par [Votre nom] Il y a X jours**

**📤 Upload File**

🔗 **Endpoint**
- Method: POST 🟡
- URL: http://localhost:8080/api/v1/files/upload

📖 **Description**
Permet l'upload d'un fichier (photo) avec compression automatique et génération de miniatures.

🔐 **Authentication**
JWT Token required 🔑

📥 **Input**
**Form Data:**
```
file: File (required) - Image file (JPG, PNG, WEBP)
```

**Example:**
```http
POST /api/v1/files/upload
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: multipart/form-data

file: [binary data]
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 📷 Types autorisés: JPG, PNG, WEBP
- 📏 Taille maximum: 10MB
- 🖼️ Résolution maximum: 4000x4000px

📤 **Response**

✅ **Success (201)**
```json
{
    "success": true,
    "data": {
        "id": "file-uuid-123",
        "originalName": "photo.jpg",
        "url": "/files/file-uuid-123",
        "thumbnailUrl": "/files/file-uuid-123/thumbnail",
        "size": 2048576,
        "mimeType": "image/jpeg",
        "width": 1920,
        "height": 1080
    },
    "message": "Fichier uploadé avec succès",
    "timestamp": "2025-07-09T17:30:00Z"
}
```

❌ **File Too Large (413)**
```json
{
    "success": false,
    "error": {
        "code": "FILE_TOO_LARGE",
        "message": "Le fichier dépasse la taille maximum autorisée (10MB)"
    },
    "timestamp": "2025-07-09T17:30:00Z"
}
```

📝 **Notes**
- 🗜️ Compression automatique pour optimiser le stockage
- 🖼️ Génération automatique de miniatures
- ⏰ Fichiers temporaires supprimés après 24h si non utilisés
- 🔗 ID retourné à utiliser dans les annonces

---

### 📥 Accéder à un Fichier
**Dernière modification par [Votre nom] Il y a X jours**

**📥 Get File**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/files/{fileId}

📖 **Description**
Accède à un fichier uploadé (photo) via son identifiant unique.

🔐 **Authentication**
Aucune authentification requise ❌

📥 **Input**
**Path Parameters:**
- `fileId` (string, required): Identifiant unique du fichier

**Query Parameters:**
- `thumbnail` (boolean, optional): Retourner la miniature (défaut: false)

**Example:**
```
GET /api/v1/files/file-uuid-123
GET /api/v1/files/file-uuid-123?thumbnail=true
```

✅ **Validation Rules**
- 🆔 ID fichier valide requis
- 📄 Fichier doit exister et être accessible
- 🔓 Pas d'authentification requise pour l'accès public

📤 **Response**

✅ **Success (200)**
```
Content-Type: image/jpeg
Content-Length: 2048576

[binary image data]
```

❌ **Not Found (404)**
```json
{
    "success": false,
    "error": {
        "code": "FILE_NOT_FOUND",
        "message": "Fichier introuvable"
    },
    "timestamp": "2025-07-09T17:45:00Z"
}
```

📝 **Notes**
- 🖼️ Retourne directement les données binaires de l'image
- 📏 En-têtes HTTP appropriés (Content-Type, Content-Length)
- 🔓 Accès public pour affichage dans l'app
- 🖼️ Paramètre thumbnail pour les miniatures optimisées

---

## 📊 Administration

### 📊 Statistiques Globales
**Dernière modification par [Votre nom] Il y a X jours**

**📊 Get Global Statistics**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/admin/stats

📖 **Description**
Récupère les statistiques globales de la plateforme pour le tableau de bord administrateur.

🔐 **Authentication**
JWT Token required 🔑 (Role: ADMIN)

📥 **Input**
**Query Parameters:**
Aucun paramètre requis ❌

**Example:**
```http
GET /api/v1/admin/stats
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

✅ **Validation Rules**
- 🔒 Authentification JWT requise
- 👑 Rôle ADMIN requis

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "users": {
            "total": 1250,
            "active": 1100,
            "newThisMonth": 85
        },
        "announcements": {
            "total": 2340,
            "active": 456,
            "lost": 234,
            "found": 222,
            "resolvedThisMonth": 89
        },
        "conversations": {
            "total": 890,
            "activeToday": 45
        },
        "files": {
            "totalSize": "2.5GB",
            "totalCount": 3456
        }
    },
    "timestamp": "2025-07-09T18:00:00Z"
}
```

❌ **Forbidden (403)**
```json
{
    "success": false,
    "error": {
        "code": "INSUFFICIENT_PRIVILEGES",
        "message": "Accès réservé aux administrateurs"
    },
    "timestamp": "2025-07-09T18:00:00Z"
}
```

📝 **Notes**
- 👑 Accès restreint aux utilisateurs avec rôle ADMIN
- 📈 Statistiques mises à jour en temps réel
- 📊 Parfait pour tableaux de bord et rapports
- 🔄 Données cachées pour performance

---

### 👥 Gestion des Utilisateurs (Admin)
**Dernière modification par [Votre nom] Il y a X jours**

**👥 Admin - List Users**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/admin/users

📖 **Description**
Liste tous les utilisateurs avec options de recherche et filtrage pour les administrateurs.

🔐 **Authentication**
JWT Token required (ADMIN role) 👑

📥 **Input**
**Query Parameters:**
- `search` (string, optional): Recherche par nom, email
- `status` (string, optional): ACTIVE, BLOCKED, DELETED
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 20)
- `sort` (string, optional): Tri (createdAt, lastName, email)

**Example:**
```
GET /api/v1/admin/users?search=marie&status=ACTIVE&page=0&size=10&sort=createdAt,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440001",
                "email": "marie.dupont@example.com",
                "firstName": "Marie",
                "lastName": "Dupont",
                "phone": "+33123456789",
                "status": "ACTIVE",
                "role": "USER",
                "createdAt": "2025-07-01T10:30:00Z",
                "lastLoginAt": "2025-07-09T08:00:00Z",
                "announcementCount": 3,
                "conversationCount": 5
            }
        ],
        "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T19:30:00Z"
}
```

📝 **Notes**
- 👑 Réservé aux administrateurs uniquement
- 🔍 Recherche sur nom complet et email
- 📊 Inclut statistiques d'activité par utilisateur

---

### 🚫 Bloquer un Utilisateur (Admin)
**Dernière modification par [Votre nom] Il y a X jours**

**🚫 Admin - Block User**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/admin/users/{userId}/block

📖 **Description**
Bloque un utilisateur, l'empêchant de se connecter et masquant ses annonces.

🔐 **Authentication**
JWT Token required (ADMIN role) 👑

📥 **Input**
**Path Parameters:**
- `userId` (UUID, required): Identifiant de l'utilisateur

**Body Parameters:**
```json
{
    "reason": "string (required)",
    "duration": "number (optional, days)"
}
```

**Example:**
```json
PUT /api/v1/admin/users/550e8400-e29b-41d4-a716-446655440001/block
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "reason": "Contenu inapproprié répété",
    "duration": 30
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "userId": "550e8400-e29b-41d4-a716-446655440001",
        "status": "BLOCKED",
        "blockedAt": "2025-07-09T20:00:00Z",
        "blockReason": "Contenu inapproprié répété",
        "unblockAt": "2025-08-08T20:00:00Z"
    },
    "message": "Utilisateur bloqué avec succès",
    "timestamp": "2025-07-09T20:00:00Z"
}
```

📝 **Notes**
- 🚫 Utilisateur ne peut plus se connecter
- 📄 Ses annonces deviennent invisibles
- 🔓 Déblocage automatique si durée spécifiée

---

### 🗑️ Supprimer un Utilisateur (Admin)
**Dernière modification par [Votre nom] Il y a X jours**

**🗑️ Admin - Delete User**

🔗 **Endpoint**
- Method: DELETE 🔴
- URL: http://localhost:8080/api/v1/admin/users/{userId}

📖 **Description**
Supprime définitivement un compte utilisateur et toutes ses données (RGPD).

🔐 **Authentication**
JWT Token required (ADMIN role) 👑

📥 **Input**
**Path Parameters:**
- `userId` (UUID, required): Identifiant de l'utilisateur

**Example:**
```
DELETE /api/v1/admin/users/550e8400-e29b-41d4-a716-446655440001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "message": "Compte utilisateur supprimé définitivement",
    "timestamp": "2025-07-09T20:15:00Z"
}
```

📝 **Notes**
- ⚠️ Suppression définitive et irréversible
- 🗂️ Toutes données liées supprimées (annonces, messages, fichiers)
- 📝 Action tracée dans les logs admin

---

### 📋 Modération des Annonces (Admin)
**Dernière modification par [Votre nom] Il y a X jours**

**📋 Admin - List All Announcements**

🔗 **Endpoint**
- Method: GET 🟢
- URL: http://localhost:8080/api/v1/admin/announcements

📖 **Description**
Liste toutes les annonces avec outils de modération pour les administrateurs.

🔐 **Authentication**
JWT Token required (ADMIN role) 👑

📥 **Input**
**Query Parameters:**
- `status` (string, optional): ACTIVE, RESOLVED, EXPIRED, BLOCKED
- `type` (string, optional): LOST, FOUND
- `userId` (string, optional): Filtrer par auteur
- `reported` (boolean, optional): Annonces signalées uniquement
- `page` (number, optional): Numéro de page (défaut: 0)
- `size` (number, optional): Taille de page (défaut: 20)

**Example:**
```
GET /api/v1/admin/announcements?status=ACTIVE&reported=true&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440002",
                "title": "iPhone 13 Pro trouvé",
                "type": "FOUND",
                "status": "ACTIVE",
                "createdAt": "2025-07-09T08:30:00Z",
                "author": {
                    "id": "550e8400-e29b-41d4-a716-446655440001",
                    "email": "marie.dupont@example.com",
                    "firstName": "Marie",
                    "lastName": "Dupont"
                },
                "reportCount": 2,
                "lastReportAt": "2025-07-09T19:00:00Z",
                "moderationNotes": ""
            }
        ],
        "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2025-07-09T20:30:00Z"
}
```

📝 **Notes**
- 👑 Vue administrative complète
- 🚨 Indicateur de signalements
- ✍️ Notes de modération incluses

---

### 🚫 Bloquer une Annonce (Admin)
**Dernière modification par [Votre nom] Il y a X jours**

**🚫 Admin - Block Announcement**

🔗 **Endpoint**
- Method: PUT 🟡
- URL: http://localhost:8080/api/v1/admin/announcements/{announcementId}/block

📖 **Description**
Bloque une annonce inappropriée, la rendant invisible publiquement.

🔐 **Authentication**
JWT Token required (ADMIN role) 👑

📥 **Input**
**Path Parameters:**
- `announcementId` (UUID, required): Identifiant de l'annonce

**Body Parameters:**
```json
{
    "reason": "string (required)",
    "moderationNotes": "string (optional)"
}
```

**Example:**
```json
PUT /api/v1/admin/announcements/550e8400-e29b-41d4-a716-446655440002/block
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "reason": "Contenu inapproprié",
    "moderationNotes": "Photos non conformes à l'objet décrit"
}
```

📤 **Response**

✅ **Success (200)**
```json
{
    "success": true,
    "data": {
        "announcementId": "550e8400-e29b-41d4-a716-446655440002",
        "status": "BLOCKED",
        "blockedAt": "2025-07-09T20:45:00Z",
        "blockReason": "Contenu inapproprié",
        "moderationNotes": "Photos non conformes à l'objet décrit"
    },
    "message": "Annonce bloquée avec succès",
    "timestamp": "2025-07-09T20:45:00Z"
}
```

📝 **Notes**
- 🚫 Annonce devient invisible dans les recherches
- 📧 Notification automatique à l'auteur
- 📝 Historique de modération conservé

---

## 🚀 Codes de Statut HTTP

| Code | Signification | Usage |
|------|---------------|-------|
| 200 | ✅ OK | Requête réussie |
| 201 | ✅ Created | Ressource créée |
| 400 | ❌ Bad Request | Données invalides |
| 401 | ❌ Unauthorized | Non authentifié |
| 403 | ❌ Forbidden | Accès interdit |
| 404 | ❌ Not Found | Ressource introuvable |
| 409 | ❌ Conflict | Conflit (ex: email déjà utilisé) |
| 413 | ❌ Payload Too Large | Fichier trop volumineux |
| 500 | 🔴 Internal Server Error | Erreur serveur |

## 🔧 Format de Réponse Standard

Toutes les réponses suivent ce format JSON standardisé :

### ✅ Succès
```json
{
    "success": true,
    "data": { ... },
    "message": "string (optional)",
    "timestamp": "2025-07-09T10:30:00Z"
}
```

### ❌ Erreur
```json
{
    "success": false,
    "error": {
        "code": "ERROR_CODE",
        "message": "Description de l'erreur",
        "details": { ... }
    },
    "timestamp": "2025-07-09T10:30:00Z"
}
```

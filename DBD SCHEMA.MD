# 🗄️ OPOSSUM Database Schema - Version MVP

## 📊 Database Overview

**Database:** `opossum_db` (PostgreSQL)  
**Architecture:** Monolithic - Single database  
**Version:** MVP (Minimum Viable Product)  
**Tables:** 6 tables essentielles seulement

---

## 👤 Table: `users`

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **email** | varchar(255) | Unique, Not null |
| **password_hash** | varchar(255) | Not null, Hashed with bcrypt |
| **first_name** | varchar(100) | Not null, length: 2<N<50 |
| **last_name** | varchar(100) | Not null, length: 2<N<50 |
| **phone** | varchar(20) | Nullable, French format |
| **avatar_url** | varchar(500) | Nullable, Direct URL |
| **role** | varchar(20) | Not null, Default: 'USER', Enum: USER/ADMIN |
| **is_active** | boolean | Default: true |
| **is_email_verified** | boolean | Default: false |
| **email_verification_token** | varchar(255) | Nullable |
| **password_reset_token** | varchar(255) | Nullable |
| **password_reset_expires_at** | timestamp | Nullable |
| **created_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |
| **updated_at** | timestamp | Default: CURRENT_TIMESTAMP, Auto-update |
| **last_login_at** | timestamp | Nullable |

### 🔑 Key Details
- **Primary Key:** id (UUID)
- **Authentication:** Email/password with JWT
- **Avatar:** Simple URL string (pas de table files complexe)
- **Verification:** Email verification token
- **Admin:** Role-based access for moderation

---

## 🔄 Table: `refresh_tokens`

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **user_id** | UUID | Not null, FK → users(id), CASCADE |
| **token** | varchar(255) | Unique, Not null |
| **expires_at** | timestamp | Not null |
| **created_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |
| **is_revoked** | boolean | Default: false |

### 🔑 Key Details
- **Purpose:** JWT refresh mechanism as required by brief
- **Security:** Token revocation support
- **Expiration:** 7 days default

---

## 📣 Table: `annonces`

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **title** | varchar(200) | Not null, length: 5<N<200 |
| **description** | text | Not null, length: 10<N<2000 |
| **type** | varchar(10) | Not null, Enum: LOST/FOUND |
| **category** | varchar(50) | Not null |
| **status** | varchar(20) | Default: 'ACTIVE', Enum: ACTIVE/RESOLVED/ARCHIVED/DELETED |
| **latitude** | decimal(10,8) | Not null, Range: -90 to 90 |
| **longitude** | decimal(11,8) | Not null, Range: -180 to 180 |
| **address** | text | Nullable |
| **city** | varchar(100) | Not null |
| **photo_url** | varchar(500) | Nullable, **UNE SEULE PHOTO** |
| **contact_phone** | varchar(20) | Nullable |
| **contact_email** | varchar(255) | Nullable |
| **user_id** | UUID | Not null, FK → users(id), CASCADE |
| **created_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |
| **updated_at** | timestamp | Default: CURRENT_TIMESTAMP, Auto-update |
| **resolved_at** | timestamp | Nullable |

### 🔑 Key Details
- **Primary Key:** id (UUID)
- **Geolocation:** Latitude/longitude as required
- **Photo:** Simple URL field (une seule photo pour MVP)
- **Status:** Lifecycle management
- **Contact:** Optional contact info
- **Moderation:** Admins can update status to ARCHIVED/DELETED

---

## 📁 Table: `files` (Optionnelle pour MVP)

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **original_name** | varchar(255) | Not null |
| **stored_name** | varchar(255) | Not null, Unique generated name |
| **file_path** | varchar(500) | Not null, Relative path |
| **mime_type** | varchar(100) | Not null |
| **file_size** | integer | Not null, Max: 10MB |
| **uploaded_by** | UUID | Not null, FK → users(id), CASCADE |
| **is_deleted** | boolean | Default: false |
| **created_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |

### 🔑 Key Details
- **Purpose:** File upload as required by brief
- **Simplification:** Pas de métadonnées complexes pour MVP
- **Usage:** URL retournée et stockée dans annonces.photo_url

---

## 💬 Table: `conversations`

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **annonce_id** | UUID | Not null, FK → annonces(id), CASCADE |
| **user1_id** | UUID | Not null, FK → users(id), CASCADE |
| **user2_id** | UUID | Not null, FK → users(id), CASCADE |
| **created_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |
| **updated_at** | timestamp | Default: CURRENT_TIMESTAMP, Auto-update |

### 🔑 Key Details
- **Purpose:** "Discussions 1-1 entre utilisateurs" comme demandé
- **Participants:** user1 = owner annonce, user2 = interested party
- **Constraint:** UNIQUE(annonce_id, user1_id, user2_id)

---

## 💬 Table: `messages`

| Key | Type | Options |
|-----|------|---------|
| **id** | UUID | Primary Key, Default: gen_random_uuid() |
| **conversation_id** | UUID | Not null, FK → conversations(id), CASCADE |
| **sender_id** | UUID | Not null, FK → users(id), CASCADE |
| **content** | text | Not null |
| **is_read** | boolean | Default: false |
| **sent_at** | timestamp | Not null, Default: CURRENT_TIMESTAMP |

### 🔑 Key Details
- **Purpose:** "Historique par annonce" comme demandé
- **Content:** Text messages only for MVP
- **Read Status:** Basic unread tracking

---

## 🚀 Ce qui est **SUPPRIMÉ** pour MVP :

### ❌ **Tables supprimées :**
- **`annonce_photos`** - Trop complexe, une seule photo suffit
- **`reports`** - Pas dans le brief initial
- **Métadonnées avancées** dans files (width, height, etc.)

### ❌ **Fonctionnalités simplifiées :**
- **Photos multiples** → Une seule photo par annonce
- **Signalements** → Phase 3 (si temps)
- **Modération avancée** → Simple changement de statut par admin
- **Chiffrement messages** → Phase 3 (si temps)

---

## ✅ **Ce qui respecte EXACTEMENT le brief :**

### **Fonctionnalités utilisateur :**
- ✅ Authentification (JWT + refresh)
- ✅ Annonces CRUD avec géoloc
- ✅ Une photo par annonce
- ✅ Messagerie 1-1
- ✅ Filtrage et recherche

### **Fonctionnalités admin :**
- ✅ Statistiques (via queries)
- ✅ Gestion utilisateurs (bloquer = is_active = false)
- ✅ Modération annonces (changer status)

---

## 📊 Relations simplifiées :

```
users (1) ←→ (N) annonces
users (1) ←→ (N) refresh_tokens
users (1) ←→ (N) conversations (as user1_id/user2_id)
users (1) ←→ (N) messages
users (1) ←→ (N) files

annonces (1) ←→ (N) conversations
conversations (1) ←→ (N) messages
```

---

## 🎯 **Avantages de cette approche MVP :**

- ✅ **6 tables seulement** au lieu de 8
- ✅ **Respecte 100% le brief**
- ✅ **Développement plus rapide**
- ✅ **Moins de complexité**
- ✅ **Fonctionnel pour la démonstration**
- ✅ **Extensible** si besoin plus tard

---

## 📝 **Note importante :**
Cette version MVP couvre **TOUTES** les fonctionnalités demandées dans le brief, mais de manière simplifiée. Vous pouvez toujours ajouter des tables comme `annonce_photos` ou `reports` en Phase 2/3 si vous avez le temps ! 🚀

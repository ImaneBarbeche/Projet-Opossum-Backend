-- =========================================
-- OPOSSUM DATABASE - Script de création complet
-- Version: MVP (6 tables essentielles)
-- Database: PostgreSQL
-- =========================================

-- 1. Créer la base de données (à exécuter en tant que superuser)
-- CREATE DATABASE opossum_db
--     WITH OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'French_France.1252'
--     LC_CTYPE = 'French_France.1252'
--     TABLESPACE = pg_default;

-- 2. Se connecter à la base opossum_db puis exécuter le reste

-- Activer l'extension UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================
-- TABLE 1: users
-- =========================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL CHECK (length(first_name) >= 2 AND length(first_name) <= 50),
    last_name VARCHAR(100) NOT NULL CHECK (length(last_name) >= 2 AND length(last_name) <= 50),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Index pour optimiser les recherches
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- =========================================
-- TABLE 2: refresh_tokens
-- =========================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN DEFAULT false,
    
    -- Foreign Key
    CONSTRAINT fk_refresh_tokens_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index pour optimiser les recherches
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- =========================================
-- TABLE 3: annonces
-- =========================================
CREATE TABLE annonces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(200) NOT NULL CHECK (length(title) >= 5 AND length(title) <= 200),
    description TEXT NOT NULL CHECK (length(description) >= 10 AND length(description) <= 2000),
    type VARCHAR(10) NOT NULL CHECK (type IN ('LOST', 'FOUND')),
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RESOLVED', 'ARCHIVED', 'DELETED')),
    latitude DECIMAL(10,8) NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
    longitude DECIMAL(11,8) NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
    address TEXT,
    city VARCHAR(100) NOT NULL,
    photo_url VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_annonces_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index pour optimiser les recherches
CREATE INDEX idx_annonces_type ON annonces(type);
CREATE INDEX idx_annonces_category ON annonces(category);
CREATE INDEX idx_annonces_status ON annonces(status);
CREATE INDEX idx_annonces_city ON annonces(city);
CREATE INDEX idx_annonces_user_id ON annonces(user_id);
CREATE INDEX idx_annonces_created_at ON annonces(created_at);
CREATE INDEX idx_annonces_location ON annonces(latitude, longitude);

-- =========================================
-- TABLE 4: files (Optionnelle pour MVP)
-- =========================================
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size INTEGER NOT NULL CHECK (file_size <= 10485760), -- 10MB max
    uploaded_by UUID NOT NULL,
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_files_user 
        FOREIGN KEY (uploaded_by) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index pour optimiser les recherches
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_files_stored_name ON files(stored_name);

-- =========================================
-- TABLE 5: conversations
-- =========================================
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    annonce_id UUID NOT NULL,
    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_conversations_annonce 
        FOREIGN KEY (annonce_id) 
        REFERENCES annonces(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user1 
        FOREIGN KEY (user1_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user2 
        FOREIGN KEY (user2_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Contrainte unique : une seule conversation par annonce entre 2 utilisateurs
    CONSTRAINT unique_conversation 
        UNIQUE(annonce_id, user1_id, user2_id),
    
    -- Vérifier que user1 et user2 sont différents
    CONSTRAINT check_different_users 
        CHECK (user1_id != user2_id)
);

-- Index pour optimiser les recherches
CREATE INDEX idx_conversations_annonce_id ON conversations(annonce_id);
CREATE INDEX idx_conversations_user1_id ON conversations(user1_id);
CREATE INDEX idx_conversations_user2_id ON conversations(user2_id);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at);

-- =========================================
-- TABLE 6: messages
-- =========================================
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL CHECK (length(content) >= 1 AND length(content) <= 1000),
    is_read BOOLEAN DEFAULT false,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_messages_conversation 
        FOREIGN KEY (conversation_id) 
        REFERENCES conversations(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender 
        FOREIGN KEY (sender_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index pour optimiser les recherches
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);
CREATE INDEX idx_messages_is_read ON messages(is_read);

-- =========================================
-- TRIGGERS pour updated_at automatique
-- =========================================

-- Fonction pour mettre à jour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers pour users
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Triggers pour annonces
CREATE TRIGGER update_annonces_updated_at 
    BEFORE UPDATE ON annonces 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Triggers pour conversations
CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON conversations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- =========================================
-- DONNÉES DE TEST (Optionnel)
-- =========================================

-- Insérer un admin par défaut
INSERT INTO users (email, password_hash, first_name, last_name, role, is_email_verified) 
VALUES (
    'admin@opossum.com',
    '$2a$10$example.hash.for.testing.only',  -- Vous devrez hasher un vrai mot de passe
    'Admin',
    'OPOSSUM',
    'ADMIN',
    true
);

-- Insérer un utilisateur de test
INSERT INTO users (email, password_hash, first_name, last_name, is_email_verified) 
VALUES (
    'test@example.com',
    '$2a$10$example.hash.for.testing.only',  -- Vous devrez hasher un vrai mot de passe
    'Marie',
    'Dupont',
    true
);

-- =========================================
-- VUES UTILES (Optionnel)
-- =========================================

-- Vue pour les statistiques admin
CREATE VIEW admin_stats AS
SELECT 
    (SELECT COUNT(*) FROM users WHERE is_active = true) as active_users,
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM annonces WHERE status = 'ACTIVE') as active_announcements,
    (SELECT COUNT(*) FROM annonces) as total_announcements,
    (SELECT COUNT(*) FROM conversations) as total_conversations,
    (SELECT COUNT(*) FROM messages WHERE sent_at >= CURRENT_DATE) as messages_today;

-- Vue pour les annonces avec infos utilisateur
CREATE VIEW annonces_with_user AS
SELECT 
    a.*,
    u.first_name,
    u.last_name,
    u.avatar_url
FROM annonces a
JOIN users u ON a.user_id = u.id
WHERE a.status = 'ACTIVE' AND u.is_active = true;

-- =========================================
-- PERMISSIONS (SÉCURITÉ - Optionnel pour développement)
-- =========================================

-- 🚨 POURQUOI CETTE SECTION ?
-- Par défaut, votre app Spring Boot va se connecter avec l'utilisateur "postgres"
-- qui a TOUS les droits (superuser). C'est OK pour le développement mais 
-- DANGEREUX en production !

-- 🎯 PRINCIPE DE SÉCURITÉ :
-- Créer un utilisateur dédié qui a SEULEMENT les droits nécessaires
-- pour l'application (pas de DROP DATABASE, pas de CREATE USER, etc.)

-- 🔧 POUR LE DÉVELOPPEMENT (ce que vous faites maintenant) :
-- Vous pouvez IGNORER cette section et utiliser directement "postgres"
-- dans votre application.properties Spring Boot :
-- spring.datasource.username=postgres
-- spring.datasource.password=votre_mot_de_passe_postgres

-- 🚀 POUR LA PRODUCTION (plus tard) :
-- Décommentez et adaptez les lignes ci-dessous :

-- 1. Créer un utilisateur spécifique pour l'app
-- CREATE USER opossum_app WITH PASSWORD '#######################!';

-- 2. Lui donner accès à la base de données
-- GRANT CONNECT ON DATABASE opossum_db TO opossum_app;

-- 3. Lui donner accès au schéma public
-- GRANT USAGE ON SCHEMA public TO opossum_app;

-- 4. Lui donner les droits sur toutes les tables
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO opossum_app;

-- 5. Lui donner accès aux séquences (pour les UUID)
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO opossum_app;

-- 📝 PUIS dans application.properties (en prod) :
-- spring.datasource.username=opossum_app
-- spring.datasource.password=mot_de_passe_super_securise_123!

-- ⚠️ POUR LE MOMENT : Gardez postgres en développement, c'est plus simple !

-- =========================================
-- VERIFICATION FINALE
-- =========================================

-- Vérifier que toutes les tables sont créées
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- Afficher le schéma de chaque table
-- \d+ users
-- \d+ refresh_tokens
-- \d+ annonces
-- \d+ files
-- \d+ conversations
-- \d+ messages

COMMIT;

# 🦘 OPOSSUM Backend

> **API REST pour l'application OPOSSUM - Objets perdus/trouvés**

## 📋 Description

OPOSSUM Backend est une API REST développée avec Spring Boot pour gérer une application d'objets perdus/trouvés. L'application permet aux utilisateurs de :

- 🔐 S'authentifier avec JWT
- 📍 Publier des annonces géolocalisées
- 💬 Communiquer via un système de messagerie
- 📧 Recevoir des notifications par email
- 🔍 Rechercher des objets à proximité

## 🛠️ Technologies utilisées

### Framework principal
- **Spring Boot 3.2.1** - Framework principal
- **Java 17** - Langage de programmation
- **Maven** - Gestionnaire de dépendances

### Base de données
- **PostgreSQL** - Base de données principale
- **Spring Data JPA** - ORM et repositories
- **H2 Database** - Base de données en mémoire pour les tests

### Sécurité
- **Spring Security** - Authentification et autorisation
- **JWT (JSON Web Tokens)** - Gestion des tokens
- **BCrypt** - Hachage des mots de passe

### Fonctionnalités
- **Spring Boot Actuator** - Monitoring et métriques
- **Spring Boot Mail** - Envoi d'emails
- **Spring Boot Cache** - Cache avec Caffeine
- **Spring Boot Validation** - Validation des données
- **ModelMapper** - Conversion entité-DTO

### Documentation
- **SpringDoc OpenAPI** - Documentation API Swagger
- **Swagger UI** - Interface de test de l'API

### Outils de développement
- **Spring Boot DevTools** - Rechargement automatique
- **Lombok** - Réduction du code boilerplate
- **Apache Commons Lang** - Utilitaires Java

## 🚀 Installation et démarrage

### Prérequis
- Java 17 ou supérieur
- Maven 3.8 ou supérieur
- PostgreSQL 12 ou supérieur

### 1. Cloner le projet
```bash
git clone https://github.com/your-org/opossum-backend.git
cd opossum-backend
```

### 2. Configuration de la base de données
Créez une base de données PostgreSQL :
```sql
CREATE DATABASE opossum_db;
CREATE USER opossum_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE opossum_db TO opossum_user;
```

### 3. Configuration des variables d'environnement
```bash
# Copiez le fichier d'exemple
cp .env.example .env

# Éditez le fichier .env avec vos valeurs
nano .env
```

### 4. Installation des dépendances
```bash
mvn clean install
```

### 5. Démarrage de l'application
```bash
# Profil développement (H2 en mémoire)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Profil production (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🔧 Configuration

### Profils Spring Boot

#### Développement (`dev`)
- Base de données H2 en mémoire
- Console H2 activée sur `/h2-console`
- Logs détaillés
- Emails désactivés

#### Test (`test`)
- Base de données H2 en mémoire
- Logs minimaux
- Fonctionnalités de sécurité allégées

#### Production (`prod`)
- Base de données PostgreSQL
- Logs optimisés
- Sécurité renforcée
- Cache amélioré

### Variables d'environnement principales

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DB_USERNAME` | Nom d'utilisateur PostgreSQL | `opossum_user` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | - |
| `JWT_SECRET` | Clé secrète JWT | - |
| `MAIL_HOST` | Serveur SMTP | `smtp.gmail.com` |
| `MAIL_USERNAME` | Email d'expédition | - |
| `MAIL_PASSWORD` | Mot de passe email | - |
| `UPLOAD_PATH` | Chemin de stockage des fichiers | `./uploads` |

## 📖 Documentation de l'API

### Accès à la documentation
- **Swagger UI** : `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8080/api/api-docs`

### Endpoints principaux

#### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion
- `POST /api/auth/refresh` - Renouvellement du token

#### Annonces
- `GET /api/annonces` - Liste des annonces
- `POST /api/annonces` - Créer une annonce
- `GET /api/annonces/{id}` - Détails d'une annonce
- `PUT /api/annonces/{id}` - Modifier une annonce
- `DELETE /api/annonces/{id}` - Supprimer une annonce

#### Géolocalisation
- `GET /api/geo/nearby` - Objets à proximité
- `GET /api/geo/distance` - Calculer une distance
- `POST /api/geo/reverse` - Géocodage inverse

#### Messagerie
- `GET /api/messages` - Messages de l'utilisateur
- `POST /api/messages` - Envoyer un message
- `GET /api/messages/{id}` - Détails d'un message

#### Utilisateurs
- `GET /api/users/profile` - Profil utilisateur
- `PUT /api/users/profile` - Modifier le profil
- `POST /api/users/avatar` - Changer l'avatar

## 🧪 Tests

### Lancer les tests
```bash
# Tous les tests
mvn test

# Tests avec couverture
mvn test jacoco:report

# Tests d'intégration uniquement
mvn test -Dtest=**/*IntegrationTest

# Tests unitaires uniquement
mvn test -Dtest=**/*UnitTest
```

### Structure des tests
```
src/test/java/
├── com/opossum/
│   ├── unit/           # Tests unitaires
│   ├── integration/    # Tests d'intégration
│   └── e2e/           # Tests end-to-end
```

## 📊 Monitoring

### Actuator endpoints
- `GET /api/actuator/health` - État de santé
- `GET /api/actuator/info` - Informations sur l'application
- `GET /api/actuator/metrics` - Métriques de performance
- `GET /api/actuator/env` - Variables d'environnement

### Logs
- **Développement** : Console + fichier `logs/opossum-backend.log`
- **Production** : Fichier `/var/log/opossum/opossum-backend.log`

## 🔒 Sécurité

### Authentification
- **JWT** avec expiration configurable
- **Refresh tokens** pour le renouvellement
- **BCrypt** pour le hachage des mots de passe

### Autorisation
- **Rôles** : `USER`, `ADMIN`
- **Permissions** par endpoint
- **Rate limiting** configurable

### CORS
- Origines autorisées configurables
- Headers et méthodes personnalisables

## 🚀 Déploiement

### Docker
```bash
# Construction de l'image
docker build -t opossum-backend:latest .

# Démarrage avec Docker Compose
docker-compose up -d
```

### Production
```bash
# Compilation pour la production
mvn clean package -Pprod

# Démarrage
java -jar target/opossum-backend-1.0.0.jar --spring.profiles.active=prod
```

## 📁 Structure du projet

```
opossum-backend/
├── src/main/java/com/opossum/
│   ├── config/         # Configuration Spring
│   ├── controller/     # Contrôleurs REST
│   ├── service/        # Services métier
│   ├── repository/     # Repositories JPA
│   ├── entity/         # Entités JPA
│   ├── dto/           # Data Transfer Objects
│   ├── mapper/        # Mappers entité-DTO
│   ├── security/      # Configuration sécurité
│   ├── exception/     # Gestion des exceptions
│   └── util/          # Utilitaires
├── src/main/resources/
│   ├── application.yml
│   ├── templates/     # Templates email
│   └── static/        # Fichiers statiques
└── src/test/java/     # Tests
```

## 🤝 Contribution

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité
3. Commitez vos modifications
4. Poussez vers la branche
5. Ouvrez une Pull Request

## 📝 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👥 Équipe

- **Équipe OPOSSUM** - Développement initial
- **Contact** : contact@opossum.fr

## 📞 Support

Pour toute question ou problème :
- 📧 Email : support@opossum.fr
- 🐛 Issues : [GitHub Issues](https://github.com/your-org/opossum-backend/issues)
- 📖 Documentation : [Wiki](https://github.com/your-org/opossum-backend/wiki)

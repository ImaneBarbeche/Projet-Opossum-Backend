# Tutoriel : Configurer Docker pour la base de données PostgreSQL

## Prérequis
- Installer Docker Desktop : https://www.docker.com/products/docker-desktop/
- Cloner ce dépôt sur votre machine

## Étapes

### 1. Copier le fichier d'environnement
Copiez `.env.example` en `.env` à la racine du projet et complétez les valeurs si besoin :

```bash
cp .env.example .env
```

### 2. Lancer la base de données PostgreSQL avec Docker
À la racine du projet, lancez :

```bash
docker-compose up -d
```

Cela démarre un conteneur PostgreSQL accessible sur le port 5432.


### 3. Vérifier que la base fonctionne

```bash
docker ps
```
Vous devez voir un conteneur `postgres` en fonctionnement.

### 3bis. Tester la connexion à la base de données


#### Avec pgAdmin4
1. Ouvrez pgAdmin4.
2. Faites un clic droit sur "Servers" > "Create" > "Server...".
3. Onglet "General" : donnez un nom (ex : OpossumDB).
4. Onglet "Connection" :
   - Host name/address : `localhost`
   - Port : `5432`
   - Maintenance database : `railway`
   - Username : `postgres`
   - Password : `your-railway-password`
5. Cliquez sur "Save". Si la connexion s'établit sans erreur, la base fonctionne !

#### Avec DBeaver (ou autre client graphique)
1. Ouvrez DBeaver et créez une nouvelle connexion PostgreSQL.
2. Renseignez :
   - Host : `localhost`
   - Port : `5432`
   - Database : `railway`
   - User : `postgres`
   - Password : `your-railway-password`
3. Cliquez sur "Test de connexion". Si tout est vert, la base fonctionne !

#### En ligne de commande (psql)
Si vous avez `psql` installé :

```bash
psql -h localhost -U postgres -d railway
```
Entrez le mot de passe quand demandé. Si vous voyez `railway=#`, la connexion est OK.

### 4. Configurer l'application
L'application est déjà configurée pour se connecter à la base Docker via le fichier `.env` :
- `DB_HOST=db`
- `DB_PORT=5432`
- `DB_NAME=railway`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=your-railway-password`

### 5. Lancer l'application
Lancez votre application comme d'habitude (Maven, Spring Boot, etc.).

### 6. Arrêter la base de données
Pour arrêter le conteneur :

```bash
docker-compose down
```

## Remarques
- Docker doit être lancé pour que l'application accède à la base.
- Si vous modifiez les identifiants dans `.env`, pensez à les reporter dans `docker-compose.yml` et inversement.
- Pour accéder à la base avec un client (DBeaver, psql, etc.) :
  - Host : `localhost`
  - Port : `5432`
  - User : `postgres`
  - Password : `your-railway-password`
  - Database : `railway`

---
Pour toute question, contactez le référent technique du projet.

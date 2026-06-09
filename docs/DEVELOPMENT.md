# TaskTracker Backend — Developer Guide

[Main README](../README.md) | [English version](README_EN.md) | [Русская версия](README_RU.md)

## Overview

This document explains how to run the TaskTracker backend locally.

## Requirements

- Java 17
- Docker
- Docker Compose
- Git
- IntelliJ IDEA
- Gradle Wrapper included in the project

## Project Setup

Clone the repository:

```bash
git clone <repository-url>
cd onyx-be
```

If the project is already cloned, open the backend folder in IntelliJ IDEA.

## Database

The project uses MySQL.

Docker Compose file:

```text
docker/docker-compose.yml
```

Start MySQL:

```bash
cd docker
docker compose up -d
```

Check running containers:

```bash
docker ps
```

Stop MySQL:

```bash
cd docker
docker compose down
```

## Database Configuration

The Docker Compose file defines the local MySQL container.

Current Docker Compose values:

```text
MYSQL_DATABASE=mydb
MYSQL_USER=admin
MYSQL_PASSWORD=myadmin
MYSQL_ROOT_PASSWORD=rootpassword
PORT=3306
```

Before running the application, make sure that the database settings in the active Spring profile match your local MySQL container.

Configuration file:

```text
src/main/resources/application-dev.yml
```

Example environment variables:

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=mydb
DB_USERNAME=admin
DB_PASSWORD=myadmin
```

## Run Application

From the project root:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

On Windows PowerShell:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

## Run Tests

From the project root:

```bash
./gradlew test
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
```

## Build Project

From the project root:

```bash
./gradlew clean build
```

On Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

## Swagger UI

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

## Useful Endpoints

### Authentication

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
POST /api/v1/auth/logout
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### Users

```text
POST  /api/v1/users/register
GET   /api/v1/users/confirm/{code}
GET   /api/v1/users/all
GET   /api/v1/users/me
PATCH /api/v1/users/me
PATCH /api/v1/users/me/password
PATCH /api/v1/users/me/avatar
```

### Projects

```text
POST   /api/v1/projects
GET    /api/v1/projects
GET    /api/v1/projects/{id}
PATCH  /api/v1/projects/{id}
DELETE /api/v1/projects/{id}
```

### Tasks

```text
POST   /api/v1/tasks
GET    /api/v1/tasks/{id}
GET    /api/v1/tasks/project/{projectId}
PATCH  /api/v1/tasks/{id}
PATCH  /api/v1/tasks/{id}/status
DELETE /api/v1/tasks/{id}
```

## Notes

The application uses JWT authentication with access and refresh tokens stored in HTTP-only cookies.

For local development, use the `dev` Spring profile.

Email and S3-compatible storage values may require local environment variables or fake development values depending on the current configuration.

# TaskTracker Backend — English Version

[Main README](../README.md) | [Русская версия](README_RU.md) | [Developer guide](DEVELOPMENT.md)

## Overview

TaskTracker Backend is a REST API for a task management application.

The backend provides user authentication, authorization, project management, task management, user profile functionality, email confirmation, password reset, avatar upload, validation, and API documentation.

This repository contains the backend part of the TaskTracker team project.

## Main Features

### Authentication and Authorization

- User registration
- Email confirmation
- Login
- Logout
- JWT-based authentication
- Access token and refresh token flow
- Tokens stored in HTTP-only cookies
- Password reset via email
- Role-based access control

### Users

- Register a new user
- Confirm user registration by email code
- Get current user profile
- Update current user profile
- Change current user password
- Upload or update user avatar
- Get all users, available for admin users

### Projects

- Create a project
- Get all projects available to the authenticated user
- Get project by ID
- Update project
- Delete project
- Restrict project access by ownership and project access rules

### Tasks

- Create a task
- Get task by ID
- Get all tasks by project
- Update task
- Update task status
- Delete task
- Restrict task access by project access rules

### Email

- Registration confirmation email
- Password reset email
- Freemarker email templates

### File Upload

- Avatar upload support
- S3-compatible storage configuration

### API Documentation

- Swagger UI
- OpenAPI configuration

## Tech Stack

- Java 17
- Spring Boot 3.4.4
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Gradle
- JWT
- HTTP-only cookies
- Bean Validation
- MapStruct
- Lombok
- Spring Mail
- Freemarker
- AWS SDK S3
- Swagger / OpenAPI
- Docker Compose
- GitHub Actions

## API Overview

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Login user |
| POST | `/api/v1/auth/refresh-token` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout user |
| POST | `/api/v1/auth/forgot-password` | Send password reset email |
| POST | `/api/v1/auth/reset-password` | Reset password |

### Users

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/users/register` | Register new user |
| GET | `/api/v1/users/confirm/{code}` | Confirm user registration |
| GET | `/api/v1/users/all` | Get all users |
| GET | `/api/v1/users/me` | Get current user profile |
| PATCH | `/api/v1/users/me` | Update current user profile |
| PATCH | `/api/v1/users/me/password` | Change current user password |
| PATCH | `/api/v1/users/me/avatar` | Upload or update avatar |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/projects` | Create project |
| GET | `/api/v1/projects` | Get all accessible projects |
| GET | `/api/v1/projects/{id}` | Get project by ID |
| PATCH | `/api/v1/projects/{id}` | Update project |
| DELETE | `/api/v1/projects/{id}` | Delete project |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/tasks` | Create task |
| GET | `/api/v1/tasks/{id}` | Get task by ID |
| GET | `/api/v1/tasks/project/{projectId}` | Get tasks by project |
| PATCH | `/api/v1/tasks/{id}` | Update task |
| PATCH | `/api/v1/tasks/{id}/status` | Update task status |
| DELETE | `/api/v1/tasks/{id}` | Delete task |

## Swagger UI

After starting the application, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Project Status

This is a team educational project. The backend includes authentication, authorization, project management, task management, user profile features, email functionality, file upload configuration, and API documentation.

## Additional Documentation

- [Main README](../README.md)
- [Русская версия](README_RU.md)
- [Developer guide](DEVELOPMENT.md)


# Friend Finder

Friend Finder is a simple social media web application inspired by platforms like Facebook.

The project was built as an MVP for a Java Backend Diploma graduation project. It allows users to create accounts, connect with other users, publish posts, interact with posts, and manage their profiles.

## Features

- User registration and login
    
- JWT-based authentication
    
- Password hashing using BCrypt
    
- Create and delete posts
    
- Text, image, and video post support
    
- Like and unlike posts
    
- Add, edit, and delete comments
    
- Friend requests
    
- Accept, reject, cancel, and unfriend
    
- Friends list
    
- Friend suggestions
    
- User profiles
    
- Profile and cover pictures
    
- Update profile information
    
- User activity log
    
- Notifications for friend requests, likes, and comments
    
- Search users by name
    
- Paginated search results
    
- Paginated posts
    
- Global posts feed sorted by newest first
    
- User-specific posts
    
- Basic validation and error handling
    

## Project Structure

The repository is organized as a Mono-repo with the frontend and backend kept in separate folders.

```text
Friend-Finder/
│
├── frontend/
│   └── Angular application
│
├── backend/
│   └── Spring Boot application
│
└── README.md
```

## Technologies Used

### Backend

- Java 17
    
- Spring Boot
    
- Spring Security
    
- Spring Data JPA
    
- JWT
    
- Hibernate
    
- Maven
    
- Oracle Database 21c
    
- Lombok
### Frontend

- Angular
    
- TypeScript
    
- Bootstrap
    
- Angular CLI
    
- Node.js v24.13.0
## Backend Architecture

The backend follows a simple layered structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Oracle Database
```

Controllers handle HTTP requests, services contain the application logic, and repositories communicate with the database using Spring Data JPA.

The backend also uses DTOs to control the data sent between the frontend and backend.

## Authentication

The application uses JWT for authentication.

After a successful login or registration, the backend generates a JWT token. The frontend sends this token with protected API requests.

The backend uses a JWT authentication filter to validate the token and identify the current user.

Passwords are not stored as plain text. They are hashed using BCrypt before being saved in the database.

## Database

The project uses Oracle Database 21c.

Main database entities include:

- User
    
- Post
    
- Comment
    
- PostLike
    
- Friendship
    
- Notification

The `User` table is named `APP_USERS` because `USER` is a reserved/special identifier in Oracle.
## Running the Project

### Requirements

Before running the project, make sure the following are installed:

- Java 17
    
- Node.js v24.13.0
    
- Angular CLI
    
- Oracle Database 21c
    
- Maven

Angular CLI is required to run and build the frontend application.

### Backend Setup

1. Clone the repository.
    
2. Open the backend folder.
    
3. Configure the Oracle database connection and JWT settings in the application configuration.
    
4. Make sure Oracle Database 21c is running.
    
5. Run the Spring Boot application using IntelliJ IDEA or Maven.
    
The backend will start as a Spring Boot application.

### Frontend Setup

1. Open the frontend folder.
    
2. Install the project dependencies:
    

```bash
npm install
```

3. Start the Angular development server:
    

```bash
ng serve
```

4. Open the local address shown by Angular in your browser.
    

## Notes

This project is an MVP. The main goal was to implement the core social media functionality and connect the Angular frontend with the Spring Boot backend and Oracle database.

Some parts of the application are intentionally kept simple compared to a production social media platform.

# Authero

## Spring Boot OAuth 2.0 Authentication with RBAC and JWT

This project is a Spring Boot-based authentication server with **Role-Based Access Control (RBAC)**, **JSON Web Tokens (JWT)**, and OAuth 2.0 integrations for **GitHub** and **Google** authentication. The application allows users to sign up, log in, and authenticate via both email/password and external OAuth providers like GitHub and Google. Additionally, it provides user role management (Admin/User) and issues JWTs upon successful authentication.

## Features

- **User Signup and Login**: Supports traditional signup with email and password.
- **Role-Based Access Control (RBAC)**: Admin and User roles are supported.
- **JSON Web Tokens (JWT)**: Secure JWT tokens are issued upon authentication for stateless communication.
- **GitHub OAuth 2.0 Integration**: Login with GitHub credentials using OAuth 2.0.
- **Google OAuth 2.0 Integration**: Login with Google credentials using OAuth 2.0.
- **Spring Security Integration**: Secures endpoints based on user roles.

## Setup Instructions

### Prerequisites

- Java 17 or above
- Maven
- GitHub and Google OAuth 2.0 Client IDs
- SQL (for database connection, although H2 can be used for testing)

### Steps to Run the Project

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Ommanimesh2/authero.git
   cd authero
   ```

2. **Go to Google Cloud Console**
   Create a new project.
   In the Credentials section, create an OAuth 2.0 Client ID.
   Save the Client ID and Client Secret for the Google integration.

3. **Set Up GitHub OAuth 2.0**
   Go to GitHub Developer Settings.
   Create a new OAuth application.
   Save the Client ID and Client Secret for GitHub.

4. **Run the project with**
   ```bash
   mvn spring-boot:run
   ```

## Authentication Flows

**GitHub OAuth 2.0**

1. Redirect user to GitHub OAuth page for login.
2. GitHub redirects back to your app with a code.
3. Exchange the code for an access token.
4. Use the token to fetch user details from GitHub.

**Google OAuth 2.0**

1. Redirect user to Google OAuth page for login.
2. Google redirects back to your app with a code.
3. Exchange the code for an access token.
4. Use the token to fetch user details from Google.

**Predefined Auth Routes**

1. There are routes for predefined roles. a super admin is bootstrapped on application startup.
2. Super Admins can create Admins and Admins can access routes of all user information

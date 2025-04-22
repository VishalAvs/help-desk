# Cloud-Based Help Desk Management System

A cloud-native ticketing system built with Spring Boot and MongoDB to streamline customer query management. Integrated with AWS services for authentication, email notifications, and file storage, this project ensures secure and efficient support ticket handling.

## 🚀 Features

- User authentication via **AWS Cognito**
- Secure file uploads with **AWS S3**
- Automated email alerts through **AWS SES**
- RESTful APIs for ticket creation, tracking, and resolution
- Role-based access control (Admin, Support, User)
- Ticket status updates, search, and filtering
- MongoDB NoSQL backend for flexible schema management

## 🛠 Tech Stack

- **Backend:** Java, Spring Boot
- **Database:** MongoDB
- **Cloud Services:** AWS Cognito, S3, SES
- **DevOps:** Maven, Git, CI/CD Ready
- **Tools:** Postman, Swagger

## 🔐 Security Features

- JWT-based authentication and authorization
- AWS Cognito integration for secure user management
- Role-based access to API endpoints

## 📂 Project Structure

```bash
ticketing-system/
├── src/
│   ├── main/
│   │   ├── java/com/helpdesk/...
│   │   └── resources/
│   │       ├── application.properties
│   │       └── ...
├── pom.xml
└── README.md
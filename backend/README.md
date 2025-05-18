# Homework Assistant Backend

A Flask-based backend API for the Homework Assistant mobile application. This backend provides features like authentication, file upload, plagiarism detection, grammar checking, and AI-powered feedback.

## Features

- **User Authentication**: Registration, login, role-based access control
- **Assignment Management**: Upload, download, and manage assignments in various formats
- **Plagiarism Detection**: Check homework for plagiarism and view detailed reports
- **Grammar & Clarity Analysis**: AI-powered grammar checking and writing improvement suggestions
- **Instructor Feedback**: Instructors can provide comments and grades on student work
- **RESTful API**: Consistent API design for mobile integration

## Requirements

- Python 3.8+
- MySQL 5.7+ (via XAMPP or standalone)
- Required Python packages are listed in `requirements.txt`

## Setup

1. **Clone the repository**

```bash
git clone <repository-url>
cd homework-assistant-backend
```

2. **Set up a virtual environment**

```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. **Install dependencies**

```bash
pip install -r requirements.txt
```

4. **Download spaCy model** (for text analysis)

```bash
python -m spacy download en_core_web_sm
```

5. **Configure environment variables**

Create a `.env` file in the root directory with the following variables:

```
SECRET_KEY=your_secret_key_here
JWT_SECRET_KEY=your_jwt_secret_key_here
MYSQL_USER=root
MYSQL_PASSWORD=your_mysql_password
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=homework_assistant
FLASK_APP=app.py
FLASK_ENV=development
```

6. **Initialize the database**

First, create a MySQL database named `homework_assistant`, then run:

```bash
python init_db.py
```

This will create all necessary tables and initialize default roles and an admin user.

## Running the Application

Start the Flask development server:

```bash
python app.py
```

The API will be available at `http://localhost:5000/api/v1/`

## API Endpoints

### Authentication

- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - Login and receive JWT token
- `POST /api/v1/auth/init-roles` - Initialize default roles (development only)

### Users

- `GET /api/v1/users/me` - Get current user profile
- `PUT /api/v1/users/me` - Update current user profile
- `GET /api/v1/users` - Get all users (admin only)
- `GET /api/v1/users/{user_id}` - Get specific user (admin only)
- `PUT /api/v1/users/{user_id}/roles` - Update user roles (admin only)
- `PUT /api/v1/users/{user_id}/status` - Activate/deactivate user (admin only)

### Assignments

- `POST /api/v1/assignments` - Upload new assignment
- `GET /api/v1/assignments` - Get user's assignments (with filtering)
- `GET /api/v1/assignments/{assignment_id}` - Get specific assignment
- `GET /api/v1/assignments/{assignment_id}/file` - Download assignment file
- `PUT /api/v1/assignments/{assignment_id}` - Update assignment details
- `PUT /api/v1/assignments/{assignment_id}/submit` - Submit assignment
- `DELETE /api/v1/assignments/{assignment_id}` - Delete assignment

### Plagiarism

- `POST /api/v1/plagiarism/check/{assignment_id}` - Check for plagiarism
- `GET /api/v1/plagiarism/report/{assignment_id}` - Get plagiarism report

### Feedback

- `POST /api/v1/feedback/analyze/{assignment_id}` - Analyze assignment for grammar issues
- `GET /api/v1/feedback/{assignment_id}` - Get feedback for assignment
- `POST /api/v1/feedback/instructor/{assignment_id}` - Add instructor feedback (instructor only)

## Default Admin User

- Email: `admin@example.com`
- Password: `admin123` (change this in production)

## License

[MIT License](LICENSE) 
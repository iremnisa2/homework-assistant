from datetime import datetime
from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token

from app.extensions import db
from app.models.user import User, Role, UserRole
from app.utils.response import APIResponse

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')

@auth_bp.route('/register', methods=['POST'])
def register():
    """Register a new user"""
    data = request.json
    
    # Validate required fields
    required_fields = ['email', 'password', 'full_name']
    for field in required_fields:
        if field not in data:
            return APIResponse.error(f"Missing required field: {field}", 400)
    
    # Check if email already exists
    if User.query.filter_by(email=data['email']).first():
        return APIResponse.error("Email already exists", 400)
    
    # Create new user
    user = User(
        email=data['email'],
        full_name=data['full_name']
    )
    user.set_password(data['password'])
    
    # Assign default role (student)
    student_role = Role.query.filter_by(name='student').first()
    if not student_role:
        # Create student role if not exists
        student_role = Role(name='student', description='Regular student user')
        db.session.add(student_role)
        db.session.flush()
    
    # Save user to database first
    db.session.add(user)
    db.session.flush()
    
    # Now create the user role with the user id
    user_role = UserRole(user_id=user.id, role_id=student_role.id)
    db.session.add(user_role)
    db.session.commit()
    
    # Generate access token
    access_token = create_access_token(identity=str(user.id))
    
    return APIResponse.success({
        'user': user.to_dict(),
        'access_token': access_token
    }, "User registered successfully")

@auth_bp.route('/login', methods=['POST'])
def login():
    """Login and get access token"""
    data = request.json
    
    # Validate required fields
    if not data or 'email' not in data or 'password' not in data:
        return APIResponse.error("Email and password required", 400)
    
    # Find user by email
    user = User.query.filter_by(email=data['email']).first()
    
    # Verify password
    if not user or not user.verify_password(data['password']):
        return APIResponse.error("Invalid email or password", 401)
    
    # Check if user is active
    if not user.is_active:
        return APIResponse.error("Account is inactive", 401)
    
    # Update last login
    user.last_login = datetime.utcnow()
    db.session.commit()
    
    # Generate access token
    access_token = create_access_token(identity=str(user.id))
    
    return APIResponse.success({
        'user': user.to_dict(),
        'access_token': access_token
    }, "Login successful")

@auth_bp.route('/init-roles', methods=['POST'])
def init_roles():
    """Initialize default roles (development only)"""
    
    # Create default roles if they don't exist
    roles = [
        {'name': 'admin', 'description': 'System administrator with full privileges'},
        {'name': 'instructor', 'description': 'Instructor with grading privileges'},
        {'name': 'student', 'description': 'Regular student user'}
    ]
    
    created = []
    for role_data in roles:
        if not Role.query.filter_by(name=role_data['name']).first():
            role = Role(**role_data)
            db.session.add(role)
            created.append(role_data['name'])
    
    if created:
        db.session.commit()
        return APIResponse.success({'created_roles': created}, "Roles initialized")
    else:
        return APIResponse.success({}, "No new roles needed")
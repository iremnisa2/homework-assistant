from functools import wraps
from flask import request, g
from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity

from app.models.user import User
from app.utils.response import APIResponse

def get_current_user():
    """Get the current authenticated user"""
    try:
        verify_jwt_in_request()
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        return user
    except:
        return None

def login_required(f):
    """Decorator to require login for an endpoint"""
    @wraps(f)
    def decorated(*args, **kwargs):
        try:
            verify_jwt_in_request()
            user_id = get_jwt_identity()
            user = User.query.get(user_id)
            
            if not user:
                return APIResponse.error("User not found", 401)
                
            if not user.is_active:
                return APIResponse.error("User account is inactive", 401)
                
            # Set user in flask g object for later use
            g.current_user = user
            
            return f(*args, **kwargs)
        except Exception as e:  # Catch specific exception
            # Log the error for debugging
            # In a production app, use a proper logger
            print(f"Authentication error: {e}") 
            return APIResponse.error("Authentication required", 401)
    return decorated

def role_required(role_name):
    """Decorator to require specific role for an endpoint"""
    def decorator(f):
        @wraps(f)
        @login_required
        def decorated_function(*args, **kwargs):
            user = g.current_user
            
            if not user.has_role(role_name):
                return APIResponse.error(f"Role '{role_name}' required", 403)
                
            return f(*args, **kwargs)
        return decorated_function
    return decorator 
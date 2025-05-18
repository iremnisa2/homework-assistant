from flask import Blueprint, request, g
from app.extensions import db
from app.models.user import User, Role, UserRole
from app.utils.response import APIResponse
from app.utils.auth import login_required, role_required

users_bp = Blueprint('users', __name__, url_prefix='/users')

@users_bp.route('/me', methods=['GET'])
@login_required
def get_current_user():
    """Get current user profile"""
    return APIResponse.success(g.current_user.to_dict())

@users_bp.route('/me', methods=['PUT'])
@login_required
def update_current_user():
    """Update current user profile"""
    data = request.json
    user = g.current_user
    
    # Fields that user can update
    allowed_fields = ['full_name', 'email', 'settings']
    
    # Update allowed fields
    for field in allowed_fields:
        if field in data:
            setattr(user, field, data[field])
    
    # Handle password change if provided
    if 'current_password' in data and 'new_password' in data:
        if not user.verify_password(data['current_password']):
            return APIResponse.error("Current password is incorrect", 400)
        
        user.set_password(data['new_password'])
    
    db.session.commit()
    
    return APIResponse.success(user.to_dict(), "Profile updated successfully")

@users_bp.route('', methods=['GET'])
@login_required
@role_required('admin')
def get_users():
    """Get all users (admin only)"""
    users = User.query.all()
    return APIResponse.success([user.to_dict() for user in users])

@users_bp.route('/<int:user_id>', methods=['GET'])
@login_required
@role_required('admin')
def get_user(user_id):
    """Get a specific user by ID (admin only)"""
    user = User.query.get_or_404(user_id)
    return APIResponse.success(user.to_dict())

@users_bp.route('/<int:user_id>/roles', methods=['PUT'])
@login_required
@role_required('admin')
def update_user_roles(user_id):
    """Update a user's roles (admin only)"""
    data = request.json
    
    if 'roles' not in data or not isinstance(data['roles'], list):
        return APIResponse.error("Roles list required", 400)
    
    user = User.query.get_or_404(user_id)
    
    # Clear existing roles
    UserRole.query.filter_by(user_id=user.id).delete()
    
    # Add new roles
    for role_name in data['roles']:
        role = Role.query.filter_by(name=role_name).first()
        if role:
            user_role = UserRole(user_id=user.id, role_id=role.id)
            db.session.add(user_role)
    
    db.session.commit()
    
    return APIResponse.success(user.to_dict(), "User roles updated")

@users_bp.route('/<int:user_id>/status', methods=['PUT'])
@login_required
@role_required('admin')
def update_user_status(user_id):
    """Activate or deactivate a user (admin only)"""
    data = request.json
    
    if 'is_active' not in data:
        return APIResponse.error("is_active field required", 400)
    
    user = User.query.get_or_404(user_id)
    
    # Cannot deactivate yourself
    if user.id == g.current_user.id and not data['is_active']:
        return APIResponse.error("Cannot deactivate your own account", 400)
    
    user.is_active = data['is_active']
    db.session.commit()
    
    status = "activated" if user.is_active else "deactivated"
    return APIResponse.success(user.to_dict(), f"User {status}") 
from flask import Blueprint, request, g
from datetime import datetime
from app.extensions import db
from app.models.assignment import Assignment
from app.utils.response import APIResponse
from app.utils.auth import login_required, role_required
from app.utils.file_handler import FileHandler

assignments_bp = Blueprint('assignments', __name__, url_prefix='/assignments')

@assignments_bp.route('', methods=['POST'])
@login_required
def create_assignment():
    """Upload a new assignment"""
    # Check if file was uploaded
    if 'file' not in request.files:
        return APIResponse.error("No file provided", 400)
        
    file = request.files['file']
    
    # Check if file has a valid filename
    if file.filename == '':
        return APIResponse.error("No file selected", 400)
        
    # Check if file type is allowed
    if not FileHandler.is_allowed_file(file.filename):
        return APIResponse.error("File type not allowed", 400)
    
    # Get form data
    title = request.form.get('title', '')
    description = request.form.get('description', '')
    deadline_str = request.form.get('deadline')
    
    # Validate title
    if not title:
        return APIResponse.error("Title is required", 400)
    
    # Parse deadline if provided
    deadline = None
    if deadline_str:
        try:
            deadline = datetime.fromisoformat(deadline_str)
        except ValueError:
            return APIResponse.error("Invalid deadline format. Use ISO format (YYYY-MM-DDTHH:MM:SS)", 400)
    
    # Save the file
    file_info = FileHandler.save_file(file, subfolder="assignments")
    
    # Create new assignment
    assignment = Assignment(
        title=title,
        description=description,
        file_path=file_info['relative_path'],
        file_name=file_info['original_filename'],
        file_type=file_info['file_type'],
        file_size=file_info['file_size'],
        deadline=deadline,
        user_id=g.current_user.id
    )
    
    # Save to database
    db.session.add(assignment)
    db.session.commit()
    
    return APIResponse.success(assignment.to_dict(), "Assignment created successfully", 201)

@assignments_bp.route('', methods=['GET'])
@login_required
def get_assignments():
    """Get all assignments for the current user"""
    # Handle query parameters for filtering
    status = request.args.get('status')
    deadline_filter = request.args.get('deadline')  # before, after, today
    search_term = request.args.get('search', '')
    
    # Base query - get user's assignments
    query = Assignment.query.filter_by(user_id=g.current_user.id)
    
    # Apply filters if provided
    if status:
        query = query.filter_by(status=status)
        
    if deadline_filter == 'before':
        query = query.filter(Assignment.deadline < datetime.utcnow())
    elif deadline_filter == 'after':
        query = query.filter(Assignment.deadline > datetime.utcnow())
    elif deadline_filter == 'today':
        today = datetime.utcnow().date()
        query = query.filter(db.func.date(Assignment.deadline) == today)
        
    if search_term:
        query = query.filter(Assignment.title.ilike(f'%{search_term}%'))
        
    # Sort by created_at desc (newest first)
    assignments = query.order_by(Assignment.created_at.desc()).all()
    
    return APIResponse.success([a.to_dict() for a in assignments])

@assignments_bp.route('/<int:assignment_id>', methods=['GET'])
@login_required
def get_assignment(assignment_id):
    """Get a specific assignment"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user has access to this assignment
    if assignment.user_id != g.current_user.id and not g.current_user.has_role('admin') and not g.current_user.has_role('instructor'):
        return APIResponse.error("Access denied", 403)
        
    return APIResponse.success(assignment.to_dict())

@assignments_bp.route('/<int:assignment_id>/file', methods=['GET'])
@login_required
def download_assignment_file(assignment_id):
    """Download the assignment file"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user has access to this assignment
    if assignment.user_id != g.current_user.id and not g.current_user.has_role('admin') and not g.current_user.has_role('instructor'):
        return APIResponse.error("Access denied", 403)
    
    # Get the file
    file = FileHandler.get_file(assignment.file_path)
    
    if not file:
        return APIResponse.error("File not found", 404)
        
    return file

@assignments_bp.route('/<int:assignment_id>', methods=['PUT'])
@login_required
def update_assignment(assignment_id):
    """Update assignment details"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user owns this assignment
    if assignment.user_id != g.current_user.id:
        return APIResponse.error("Access denied", 403)
        
    # Get JSON data
    data = request.json
    
    # Fields that can be updated
    allowed_fields = ['title', 'description', 'deadline']
    
    # Update allowed fields
    for field in allowed_fields:
        if field in data:
            if field == 'deadline' and data[field]:
                try:
                    setattr(assignment, field, datetime.fromisoformat(data[field]))
                except ValueError:
                    return APIResponse.error("Invalid deadline format. Use ISO format (YYYY-MM-DDTHH:MM:SS)", 400)
            else:
                setattr(assignment, field, data[field])
    
    db.session.commit()
    
    return APIResponse.success(assignment.to_dict(), "Assignment updated")

@assignments_bp.route('/<int:assignment_id>/submit', methods=['PUT'])
@login_required
def submit_assignment(assignment_id):
    """Mark assignment as submitted"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user owns this assignment
    if assignment.user_id != g.current_user.id:
        return APIResponse.error("Access denied", 403)
        
    # Update submission status
    assignment.is_submitted = True
    assignment.submitted_at = datetime.utcnow()
    assignment.status = 'submitted'
    
    db.session.commit()
    
    return APIResponse.success(assignment.to_dict(), "Assignment submitted")

@assignments_bp.route('/<int:assignment_id>', methods=['DELETE'])
@login_required
def delete_assignment(assignment_id):
    """Delete an assignment"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user owns this assignment
    if assignment.user_id != g.current_user.id and not g.current_user.has_role('admin'):
        return APIResponse.error("Access denied", 403)
    
    # Delete the file
    FileHandler.delete_file(assignment.file_path)
    
    # Delete from database (cascade will delete related reports/feedback)
    db.session.delete(assignment)
    db.session.commit()
    
    return APIResponse.success(None, "Assignment deleted")
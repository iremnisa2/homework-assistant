import os
from datetime import datetime
from flask import Blueprint, request, g, current_app
from app.extensions import db
from app.models.assignment import Assignment
from app.models.feedback import Feedback
from app.utils.response import APIResponse
from app.utils.auth import login_required, role_required
from app.utils.text_analyzer import TextAnalyzer
from app.utils.file_handler import FileHandler

feedback_bp = Blueprint('feedback', __name__, url_prefix='/feedback')

@feedback_bp.route('/analyze/<int:assignment_id>', methods=['POST'])
@login_required
def analyze_assignment(assignment_id):
    """Analyze assignment and provide feedback"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user has access to this assignment
    if (assignment.user_id != g.current_user.id and 
        not g.current_user.has_role('admin') and 
        not g.current_user.has_role('instructor')):
        return APIResponse.error("Access denied", 403)
    
    # Get file path
    upload_folder = current_app.config['UPLOAD_FOLDER']
    file_path = os.path.join(upload_folder, assignment.file_path)
    
    if not os.path.exists(file_path):
        return APIResponse.error("Assignment file not found", 404)
    
    # Initialize text analyzer
    analyzer = TextAnalyzer()
    
    # Extract text from file
    try:
        # This would use the text extraction function from PlagiarismDetector
        # For now, we'll simulate with a simple text read
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            text = f.read()
    except:
        # If reading fails, give feedback about the file type
        return APIResponse.error(f"Unable to extract text from {assignment.file_type} file", 400)
    
    # Analyze text
    result = analyzer.analyze_text(text)
    
    if not result['success']:
        return APIResponse.error(result.get('error', 'Failed to analyze text'), 400)
    
    # Create or update feedback
    existing_feedback = Feedback.query.filter_by(assignment_id=assignment.id).first()
    
    if existing_feedback:
        existing_feedback.grammar_issues = result['grammar_issues']
        existing_feedback.clarity_score = result['clarity_score']
        existing_feedback.structure_feedback = result['structure_feedback']
        existing_feedback.readability_score = result['readability_score']
        existing_feedback.improvement_suggestions = result['improvement_suggestions']
        existing_feedback.rewrite_suggestions = result['rewrite_suggestions']
        feedback = existing_feedback
    else:
        feedback = Feedback(
            assignment_id=assignment.id,
            grammar_issues=result['grammar_issues'],
            clarity_score=result['clarity_score'],
            structure_feedback=result['structure_feedback'],
            readability_score=result['readability_score'],
            improvement_suggestions=result['improvement_suggestions'],
            rewrite_suggestions=result['rewrite_suggestions']
        )
        db.session.add(feedback)
    
    db.session.commit()
    
    return APIResponse.success(feedback.to_dict(), "Assignment analysis completed")

@feedback_bp.route('/<int:assignment_id>', methods=['GET'])
@login_required
def get_feedback(assignment_id):
    """Get feedback for an assignment"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user has access to this assignment
    if (assignment.user_id != g.current_user.id and 
        not g.current_user.has_role('admin') and 
        not g.current_user.has_role('instructor')):
        return APIResponse.error("Access denied", 403)
    
    # Get feedback
    feedback = Feedback.query.filter_by(assignment_id=assignment.id).first()
    
    if not feedback:
        return APIResponse.error("No feedback available for this assignment", 404)
    
    return APIResponse.success(feedback.to_dict())

@feedback_bp.route('/instructor/<int:assignment_id>', methods=['POST'])
@login_required
@role_required('instructor')
def add_instructor_feedback(assignment_id):
    """Add instructor feedback to an assignment"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Get JSON data
    data = request.json
    
    if not data or 'comments' not in data:
        return APIResponse.error("Comments required", 400)
    
    # Get or create feedback
    feedback = Feedback.query.filter_by(assignment_id=assignment.id).first()
    
    if not feedback:
        feedback = Feedback(assignment_id=assignment.id)
        db.session.add(feedback)
    
    # Update instructor feedback
    feedback.instructor_comments = data['comments']
    feedback.instructor_feedback_date = datetime.utcnow()
    
    # Update grade if provided
    if 'grade' in data:
        feedback.grade = data['grade']
        # If grade provided, update assignment status
        assignment.status = 'graded'
        
    db.session.commit()
    
    return APIResponse.success(feedback.to_dict(), "Instructor feedback added") 
import os
from flask import Blueprint, request, g, current_app
from app.extensions import db
from app.models.assignment import Assignment
from app.models.plagiarism_report import PlagiarismReport
from app.utils.response import APIResponse
from app.utils.auth import login_required, role_required
from app.utils.plagiarism_detector import PlagiarismDetector

plagiarism_bp = Blueprint('plagiarism', __name__, url_prefix='/plagiarism')

@plagiarism_bp.route('/check/<int:assignment_id>', methods=['POST'])
@login_required
def check_plagiarism(assignment_id):
    """Check assignment for plagiarism"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user owns this assignment or is instructor/admin
    if (assignment.user_id != g.current_user.id and 
        not g.current_user.has_role('admin') and 
        not g.current_user.has_role('instructor')):
        return APIResponse.error("Access denied", 403)
    
    # Get file path
    upload_folder = current_app.config['UPLOAD_FOLDER']
    file_path = os.path.join(upload_folder, assignment.file_path)
    
    if not os.path.exists(file_path):
        return APIResponse.error("Assignment file not found", 404)
    
    # Initialize plagiarism detector
    detector = PlagiarismDetector()
    
    # For demonstration, we'll use sample sources
    # In a real implementation, this would come from a database of sources
    comparison_sources = {}
    
    # Optional: Get comparison sources from request
    if request.json and 'comparison_sources' in request.json:
        comparison_sources = request.json['comparison_sources']
    
    # Check for plagiarism
    result = detector.check_plagiarism(file_path, comparison_sources)
    
    if not result['success']:
        return APIResponse.error(result.get('error', 'Failed to check plagiarism'), 400)
    
    # Create or update plagiarism report
    existing_report = PlagiarismReport.query.filter_by(assignment_id=assignment.id).first()
    
    if existing_report:
        existing_report.similarity_score = result['similarity_score']
        existing_report.flagged_sections = result['flagged_sections']
        existing_report.sources = result['sources']
        existing_report.report_data = result
        report = existing_report
    else:
        report = PlagiarismReport(
            assignment_id=assignment.id,
            similarity_score=result['similarity_score'],
            flagged_sections=result['flagged_sections'],
            sources=result['sources'],
            report_data=result
        )
        db.session.add(report)
    
    db.session.commit()
    
    return APIResponse.success(report.to_dict(), "Plagiarism check completed")

@plagiarism_bp.route('/report/<int:assignment_id>', methods=['GET'])
@login_required
def get_plagiarism_report(assignment_id):
    """Get plagiarism report for assignment"""
    assignment = Assignment.query.get_or_404(assignment_id)
    
    # Check if user has access to this assignment
    if (assignment.user_id != g.current_user.id and 
        not g.current_user.has_role('admin') and 
        not g.current_user.has_role('instructor')):
        return APIResponse.error("Access denied", 403)
    
    # Get plagiarism report
    report = PlagiarismReport.query.filter_by(assignment_id=assignment.id).first()
    
    if not report:
        return APIResponse.error("No plagiarism report available for this assignment", 404)
    
    return APIResponse.success(report.to_dict()) 
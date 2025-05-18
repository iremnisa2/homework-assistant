from flask import Blueprint

def register_blueprints(app):
    """Register all API blueprints with the Flask app"""
    
    # Import blueprints here to avoid circular imports
    from app.api.auth import auth_bp
    from app.api.users import users_bp
    from app.api.assignments import assignments_bp
    from app.api.feedback import feedback_bp
    from app.api.plagiarism import plagiarism_bp
    
    # Create main API blueprint
    api_bp = Blueprint('api', __name__, url_prefix='/api/')
    
    # Register individual API modules
    api_bp.register_blueprint(auth_bp)
    api_bp.register_blueprint(users_bp)
    api_bp.register_blueprint(assignments_bp)
    api_bp.register_blueprint(feedback_bp)
    api_bp.register_blueprint(plagiarism_bp)
    
    # Register main API blueprint with app
    app.register_blueprint(api_bp) 
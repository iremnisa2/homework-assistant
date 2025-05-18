import os
from flask import Flask
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from flask_migrate import Migrate

from config import get_config
from app.extensions import db
from app.utils.response import APIResponse
from app.api import register_blueprints
from init_db import init_db_if_needed

def create_app(config=None):
    app = Flask(__name__)
    
    # Load configuration
    if config is None:
        app.config.from_object(get_config())
    else:
        app.config.from_object(config)
    
    # Ensure upload directory exists
    os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
    
    # Initialize extensions
    CORS(app)
    JWTManager(app)
    db.init_app(app)
    Migrate(app, db)
    
    # Initialize database if needed
    with app.app_context():
        init_db_if_needed()
    
    # Register blueprints
    register_blueprints(app)
    
    # Global error handlers
    @app.errorhandler(404)
    def not_found(error):
        return APIResponse.error("Resource not found", 404)
    
    @app.errorhandler(500)
    def server_error(error):
        return APIResponse.error("Internal server error", 500)
    
    return app

if __name__ == "__main__":
    app = create_app()
    app.run(host="0.0.0.0", port=5000)
import os
import sys
from datetime import datetime
from flask import Flask, current_app
from sqlalchemy import inspect
from app.extensions import db
from app.models.user import User, Role, UserRole
from config import DevelopmentConfig

def create_app():
    """Create Flask app for database initialization"""
    app = Flask(__name__)
    app.config.from_object(DevelopmentConfig)
    db.init_app(app)
    return app

def init_db():
    """Initialize database with tables and default data"""
    app = create_app()
    
    with app.app_context():
        # Create all tables
        db.create_all()
        print("Created database tables.")
        
        # Create default roles if they don't exist
        roles = [
            {'name': 'admin', 'description': 'System administrator with full privileges'},
            {'name': 'instructor', 'description': 'Instructor with grading privileges'},
            {'name': 'student', 'description': 'Regular student user'}
        ]
        
        for role_data in roles:
            if not Role.query.filter_by(name=role_data['name']).first():
                role = Role(**role_data)
                db.session.add(role)
                print(f"Created role: {role_data['name']}")
        
        # Create admin user if not exists
        admin_email = 'admin@example.com'
        
        if not User.query.filter_by(email=admin_email).first():
            admin = User(
                email=admin_email,
                full_name='Administrator',
                is_active=True,
                last_login=datetime.utcnow()
            )
            admin.set_password('admin123')  # Default password (should be changed)
            db.session.add(admin)
            db.session.flush()  # Flush to get admin ID
            
            # Assign admin role
            admin_role = Role.query.filter_by(name='admin').first()
            if admin_role:
                user_role = UserRole(user_id=admin.id, role_id=admin_role.id)
                db.session.add(user_role)
                
            print(f"Created admin user: {admin_email}")
        
        # Commit all changes
        db.session.commit()
        print("Database initialization completed.")

def init_db_if_needed():
    """Check if database needs initialization and initialize if needed"""
    # Check if tables exist
    inspector = inspect(db.engine)
    tables = inspector.get_table_names()
    
    required_tables = ['user', 'role', 'user_role']
    missing_tables = [table for table in required_tables if table not in tables]
    
    if missing_tables:
        print(f"Database missing tables: {', '.join(missing_tables)}. Initializing database...")
        # Create all tables
        db.create_all()
        
        # Create default roles
        roles = [
            {'name': 'admin', 'description': 'System administrator with full privileges'},
            {'name': 'instructor', 'description': 'Instructor with grading privileges'},
            {'name': 'student', 'description': 'Regular student user'}
        ]
        
        for role_data in roles:
            if not Role.query.filter_by(name=role_data['name']).first():
                role = Role(**role_data)
                db.session.add(role)
        
        # Create admin user if not exists
        admin_email = 'admin@example.com'
        
        if not User.query.filter_by(email=admin_email).first():
            admin = User(
                email=admin_email,
                full_name='Administrator',
                is_active=True,
                last_login=datetime.utcnow()
            )
            admin.set_password('admin123')  # Default password (should be changed)
            db.session.add(admin)
            db.session.flush()  # Flush to get admin ID
            
            # Assign admin role
            admin_role = Role.query.filter_by(name='admin').first()
            if admin_role:
                user_role = UserRole(user_id=admin.id, role_id=admin_role.id)
                db.session.add(user_role)
        
        # Commit all changes
        db.session.commit()
        print("Database initialization completed.")
    else:
        print("Database tables already exist, skipping initialization.")

if __name__ == "__main__":
    init_db()
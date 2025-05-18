from datetime import datetime
from app.extensions import db

class Assignment(db.Model):
    __tablename__ = 'assignments'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255), nullable=False)
    description = db.Column(db.Text)
    file_path = db.Column(db.String(255), nullable=False)  # Path to stored file
    file_name = db.Column(db.String(255), nullable=False)  # Original filename
    file_type = db.Column(db.String(50), nullable=False)   # File extension/MIME type
    file_size = db.Column(db.Integer, nullable=False)      # File size in bytes
    deadline = db.Column(db.DateTime)                      # Assignment deadline
    is_submitted = db.Column(db.Boolean, default=False)    # Whether formally submitted
    submitted_at = db.Column(db.DateTime)                  # When submitted
    status = db.Column(db.String(50), default='draft')     # draft, submitted, graded
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Foreign Keys
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    
    # Relationships
    user = db.relationship('User', back_populates='assignments')
    feedback = db.relationship('Feedback', back_populates='assignment', uselist=False, cascade="all, delete-orphan")
    plagiarism_report = db.relationship('PlagiarismReport', back_populates='assignment', uselist=False, cascade="all, delete-orphan")
    
    def __repr__(self):
        return f'<Assignment {self.title}>'
    
    def to_dict(self):
        """Convert assignment to dictionary"""
        return {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'file_name': self.file_name,
            'file_type': self.file_type,
            'file_size': self.file_size,
            'deadline': self.deadline.isoformat() if self.deadline else None,
            'is_submitted': self.is_submitted,
            'submitted_at': self.submitted_at.isoformat() if self.submitted_at else None,
            'status': self.status,
            'created_at': self.created_at.isoformat(),
            'updated_at': self.updated_at.isoformat(),
            'user_id': self.user_id,
            'has_plagiarism_report': self.plagiarism_report is not None,
            'has_feedback': self.feedback is not None
        } 
from datetime import datetime
from app.extensions import db

class Feedback(db.Model):
    __tablename__ = 'feedback'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Grammar and style feedback
    grammar_issues = db.Column(db.JSON)  # List of grammar issues with positions
    clarity_score = db.Column(db.Float)  # Score from 0-10 for clarity
    structure_feedback = db.Column(db.Text)  # Feedback on document structure
    readability_score = db.Column(db.Float)  # Readability score
    
    # AI suggestions
    improvement_suggestions = db.Column(db.JSON)  # Content improvement suggestions
    rewrite_suggestions = db.Column(db.JSON)  # Suggested rewrites for specific sections
    
    # Instructor feedback
    instructor_comments = db.Column(db.Text)  # Comments from instructor
    instructor_feedback_date = db.Column(db.DateTime)  # When instructor gave feedback
    grade = db.Column(db.String(10))  # Grade given by instructor (if any)
    
    # Metadata
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Foreign Keys
    assignment_id = db.Column(db.Integer, db.ForeignKey('assignments.id'), nullable=False)
    
    # Relationships
    assignment = db.relationship('Assignment', back_populates='feedback')
    
    def __repr__(self):
        return f'<Feedback for Assignment {self.assignment_id}>'
    
    def to_dict(self):
        """Convert feedback to dictionary"""
        return {
            'id': self.id,
            'grammar_issues': self.grammar_issues,
            'clarity_score': self.clarity_score,
            'structure_feedback': self.structure_feedback,
            'readability_score': self.readability_score,
            'improvement_suggestions': self.improvement_suggestions,
            'rewrite_suggestions': self.rewrite_suggestions,
            'instructor_comments': self.instructor_comments,
            'instructor_feedback_date': self.instructor_feedback_date.isoformat() if self.instructor_feedback_date else None,
            'grade': self.grade,
            'created_at': self.created_at.isoformat(),
            'updated_at': self.updated_at.isoformat(),
            'assignment_id': self.assignment_id
        } 
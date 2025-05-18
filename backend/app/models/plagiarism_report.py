from datetime import datetime
from app.extensions import db

class PlagiarismReport(db.Model):
    __tablename__ = 'plagiarism_reports'
    
    id = db.Column(db.Integer, primary_key=True)
    similarity_score = db.Column(db.Float, nullable=False)  # Overall similarity percentage (0-100)
    flagged_sections = db.Column(db.JSON)  # Sections flagged as potentially plagiarized with details
    sources = db.Column(db.JSON)  # Potential sources of plagiarized content
    report_data = db.Column(db.JSON)  # Raw data from plagiarism check
    generated_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Foreign Keys
    assignment_id = db.Column(db.Integer, db.ForeignKey('assignments.id'), nullable=False)
    
    # Relationships
    assignment = db.relationship('Assignment', back_populates='plagiarism_report')
    
    def __repr__(self):
        return f'<PlagiarismReport for Assignment {self.assignment_id}, Score: {self.similarity_score}%>'
    
    def to_dict(self):
        """Convert plagiarism report to dictionary"""
        return {
            'id': self.id,
            'similarity_score': self.similarity_score,
            'flagged_sections': self.flagged_sections,
            'sources': self.sources,
            'generated_at': self.generated_at.isoformat(),
            'updated_at': self.updated_at.isoformat(),
            'assignment_id': self.assignment_id
        } 
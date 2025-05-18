import os
import uuid
from datetime import datetime
from werkzeug.utils import secure_filename
from flask import current_app, send_file

class FileHandler:
    """Utility class for file operations"""

    @staticmethod
    def get_file_extension(filename):
        """Extract file extension from a filename"""
        return os.path.splitext(filename)[1].lower() if filename else ""
        
    @staticmethod
    def is_allowed_file(filename, allowed_extensions=None):
        """Check if a file has an allowed extension"""
        if allowed_extensions is None:
            allowed_extensions = {'.pdf', '.docx', '.doc', '.txt', '.rtf', '.odt'}
            
        ext = FileHandler.get_file_extension(filename)
        return ext in allowed_extensions
        
    @staticmethod
    def generate_unique_filename(original_filename):
        """Generate a unique filename while preserving the extension"""
        ext = FileHandler.get_file_extension(original_filename)
        timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
        random_str = str(uuid.uuid4())[:8]
        secure_name = secure_filename(original_filename)
        basename = os.path.splitext(secure_name)[0]
        
        # Return a unique filename
        return f"{basename}_{timestamp}_{random_str}{ext}"
        
    @staticmethod
    def save_file(file, subfolder=""):
        """Save an uploaded file to the upload folder"""
        # Secure the original filename
        original_filename = secure_filename(file.filename)
        
        # Generate a unique filename
        unique_filename = FileHandler.generate_unique_filename(original_filename)
        
        # Determine the upload path
        upload_folder = current_app.config['UPLOAD_FOLDER']
        if subfolder:
            upload_path = os.path.join(upload_folder, subfolder)
            os.makedirs(upload_path, exist_ok=True)
        else:
            upload_path = upload_folder
            
        # Save the file
        file_path = os.path.join(upload_path, unique_filename)
        file.save(file_path)
        
        # Calculate relative path
        relative_path = os.path.join(subfolder, unique_filename) if subfolder else unique_filename
        
        return {
            'original_filename': original_filename,
            'saved_filename': unique_filename,
            'file_path': file_path,
            'relative_path': relative_path,
            'file_size': os.path.getsize(file_path),
            'file_type': FileHandler.get_file_extension(original_filename)
        }
        
    @staticmethod
    def get_file(relative_path):
        """Retrieve a file by its relative path"""
        upload_folder = current_app.config['UPLOAD_FOLDER']
        file_path = os.path.join(upload_folder, relative_path)
        
        if not os.path.exists(file_path):
            return None
            
        return send_file(file_path)
        
    @staticmethod
    def delete_file(relative_path):
        """Delete a file by its relative path"""
        upload_folder = current_app.config['UPLOAD_FOLDER']
        file_path = os.path.join(upload_folder, relative_path)
        
        if os.path.exists(file_path):
            os.remove(file_path)
            return True
            
        return False 
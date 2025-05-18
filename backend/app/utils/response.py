from flask import jsonify

class APIResponse:
    """Standardized API response format for the application"""
    
    @staticmethod
    def success(data=None, message="Success", code=200):
        """
        Construct a success response
        
        Args:
            data: Response data
            message: Response message
            code: HTTP status code
            
        Returns:
            Flask response object
        """
        response = {
            "status": "success",
            "message": message,
            "data": data
        }
        return jsonify(response), code
    
    @staticmethod
    def error(message="Error occurred", code=400, errors=None):
        """
        Construct an error response
        
        Args:
            message: Error message
            code: HTTP status code
            errors: Additional error details
            
        Returns:
            Flask response object
        """
        response = {
            "status": "error",
            "message": message
        }
        
        if errors:
            response["errors"] = errors
            
        return jsonify(response), code 
import os
import difflib
import re
from flask import current_app
import PyPDF2
import docx

class PlagiarismDetector:
    """Utility class for plagiarism detection"""
    
    def __init__(self):
        self.threshold = 0.3  # Similarity threshold (0-1)
        self.min_chunk_size = 40  # Minimum size of text chunk to check
    
    def extract_text_from_file(self, file_path):
        """Extract text from different file types"""
        _, ext = os.path.splitext(file_path)
        ext = ext.lower()
        
        try:
            if ext == '.pdf':
                return self._extract_from_pdf(file_path)
            elif ext in ['.docx']:
                return self._extract_from_docx(file_path)
            elif ext in ['.txt', '.rtf']:
                return self._extract_from_txt(file_path)
            else:
                return ""
        except Exception as e:
            print(f"Error extracting text: {str(e)}")
            return ""
    
    def _extract_from_pdf(self, file_path):
        """Extract text from PDF file"""
        text = ""
        with open(file_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            for page_num in range(len(pdf_reader.pages)):
                page = pdf_reader.pages[page_num]
                text += page.extract_text() + "\n"
        return text
    
    def _extract_from_docx(self, file_path):
        """Extract text from DOCX file"""
        doc = docx.Document(file_path)
        full_text = []
        for para in doc.paragraphs:
            full_text.append(para.text)
        return '\n'.join(full_text)
    
    def _extract_from_txt(self, file_path):
        """Extract text from TXT file"""
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
            return file.read()
    
    def normalize_text(self, text):
        """Normalize text for comparison (lowercase, remove extra spaces)"""
        text = text.lower()
        text = re.sub(r'\s+', ' ', text)  # Replace multiple spaces with single space
        text = re.sub(r'[^\w\s]', '', text)  # Remove punctuation
        return text.strip()
    
    def split_into_chunks(self, text, chunk_size=100, overlap=50):
        """Split text into overlapping chunks for comparison"""
        words = text.split()
        chunks = []
        
        for i in range(0, len(words), chunk_size - overlap):
            chunk = ' '.join(words[i:i + chunk_size])
            if len(chunk.split()) >= self.min_chunk_size:
                chunks.append(chunk)
                
        return chunks
    
    def calculate_similarity(self, text1, text2):
        """Calculate similarity between two texts using difflib"""
        # Normalize texts
        text1 = self.normalize_text(text1)
        text2 = self.normalize_text(text2)
        
        # Use difflib to calculate similarity
        similarity_ratio = difflib.SequenceMatcher(None, text1, text2).ratio()
        
        return similarity_ratio
    
    def find_similar_chunks(self, text, comparison_sources, min_similarity=0.8):
        """Find chunks of text that are similar to comparison sources"""
        # Normalize and chunk the text
        normalized_text = self.normalize_text(text)
        chunks = self.split_into_chunks(normalized_text)
        
        flagged_sections = []
        
        # For each chunk, compare against all sources
        for i, chunk in enumerate(chunks):
            for source_name, source_text in comparison_sources.items():
                source_normalized = self.normalize_text(source_text)
                source_chunks = self.split_into_chunks(source_normalized)
                
                for source_chunk in source_chunks:
                    similarity = self.calculate_similarity(chunk, source_chunk)
                    
                    if similarity >= min_similarity:
                        # Calculate approximate position in original text
                        start_pos = normalized_text.find(chunk)
                        end_pos = start_pos + len(chunk) if start_pos != -1 else -1
                        
                        flagged_sections.append({
                            'chunk_index': i,
                            'text': chunk,
                            'source': source_name,
                            'similarity': similarity,
                            'start_pos': start_pos,
                            'end_pos': end_pos
                        })
                        
                        # Break after finding a match for this chunk
                        break
        
        return flagged_sections
    
    def check_plagiarism(self, file_path, comparison_sources=None):
        """
        Main method to check for plagiarism
        
        Args:
            file_path: Path to the file to check
            comparison_sources: Dict of source_name -> source_text for comparison
            
        Returns:
            Dict containing plagiarism results
        """
        # Extract text from file
        text = self.extract_text_from_file(file_path)
        
        if not text:
            return {
                'success': False,
                'error': 'Could not extract text from file',
                'similarity_score': 0,
                'flagged_sections': []
            }
        
        # If no comparison sources provided, use an empty dict
        if comparison_sources is None:
            comparison_sources = {}
            
        # Find similar chunks
        flagged_sections = self.find_similar_chunks(text, comparison_sources)
        
        # Calculate overall similarity score
        total_chunks = len(self.split_into_chunks(self.normalize_text(text)))
        flagged_chunks = len(set(section['chunk_index'] for section in flagged_sections))
        
        similarity_score = (flagged_chunks / total_chunks) * 100 if total_chunks > 0 else 0
        
        return {
            'success': True,
            'similarity_score': similarity_score,
            'flagged_sections': flagged_sections,
            'sources': list(comparison_sources.keys())
        } 
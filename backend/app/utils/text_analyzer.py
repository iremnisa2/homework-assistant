import re
import os
import nltk
import textwrap
from nltk.tokenize import sent_tokenize, word_tokenize
from nltk.tag import pos_tag
from nltk.corpus import stopwords
from nltk.probability import FreqDist

class TextAnalyzer:
    """Utility class for text analysis and grammar checking"""
    
    def __init__(self):
        # Download necessary nltk resources
        try:
            nltk.download('punkt', quiet=True)
            nltk.download('averaged_perceptron_tagger', quiet=True)
            nltk.download('stopwords', quiet=True)
            self.nlp_available = True
        except:
            # Graceful fallback if resources not available
            self.nlp_available = False
            print("Warning: NLTK resources not available. Limited functionality.")
    
    def analyze_text(self, text):
        """
        Analyze text for grammar issues, clarity, and structure
        
        Args:
            text: The text to analyze
            
        Returns:
            Dict containing analysis results
        """
        if not text or not self.nlp_available:
            return {
                'success': False,
                'error': 'Text empty or NLTK resources not available',
                'grammar_issues': [],
                'clarity_score': 0,
                'readability_score': 0,
                'structure_feedback': 'Analysis not available'
            }
        
        # Tokenize with NLTK
        sentences = sent_tokenize(text)
        words = word_tokenize(text)
        pos_tags = pos_tag(words)
        
        # Find grammar issues
        grammar_issues = self._find_grammar_issues(text, sentences, words, pos_tags)
        
        # Calculate clarity score
        clarity_score = self._calculate_clarity_score(words, pos_tags)
        
        # Calculate readability score
        readability_score = self._calculate_readability_score(text)
        
        # Analyze structure
        structure_feedback = self._analyze_structure(text)
        
        # Generate improvement suggestions
        improvement_suggestions = self._generate_improvement_suggestions(text, sentences, words, pos_tags)
        
        # Generate rewrite suggestions
        rewrite_suggestions = self._generate_rewrite_suggestions(sentences)
        
        return {
            'success': True,
            'grammar_issues': grammar_issues,
            'clarity_score': clarity_score,
            'readability_score': readability_score,
            'structure_feedback': structure_feedback,
            'improvement_suggestions': improvement_suggestions,
            'rewrite_suggestions': rewrite_suggestions
        }
    
    def _find_grammar_issues(self, text, sentences, words, pos_tags):
        """Find grammar issues in the text"""
        issues = []
        
        # Simple rule-based checks (basic examples)
        
        # Check for repeated words
        for i in range(1, len(words)):
            if words[i].lower() == words[i-1].lower() and words[i].isalpha():
                # Find position in text (approximate)
                position = text.lower().find(words[i].lower() + " " + words[i].lower())
                if position >= 0:
                    issues.append({
                        'type': 'repeated-word',
                        'position': {'start': position, 'end': position + len(words[i])*2 + 1},
                        'text': words[i] + " " + words[i],
                        'suggestion': f"Repeated word: '{words[i]}'"
                    })
        
        # Check for long, complex sentences
        for sent in sentences:
            sent_words = word_tokenize(sent)
            if len(sent_words) > 40:  # More than 40 tokens
                position = text.find(sent)
                issues.append({
                    'type': 'long-sentence',
                    'position': {'start': position, 'end': position + len(sent)},
                    'text': sent,
                    'suggestion': 'Consider breaking this long sentence into smaller ones'
                })
        
        return issues
    
    def _calculate_clarity_score(self, words, pos_tags):
        """Calculate clarity score (0-10)"""
        # This is a simplified score based on various linguistic features
        # In a real app, this would be more sophisticated
        
        # Count long and complex words
        complex_word_ratio = sum(1 for word in words if len(word) > 7) / len(words) if words else 0
        
        # Average sentence length (using existing tokenization)
        avg_word_len = sum(len(word) for word in words) / len(words) if words else 0
        
        # Calculate clarity score (lower complexity, higher clarity)
        clarity_score = 10 - (complex_word_ratio * 10) - (min(avg_word_len / 10, 0.5) * 10)
        
        # Ensure score is between 0-10
        return max(0, min(clarity_score, 10))
    
    def _calculate_readability_score(self, text):
        """Calculate readability score (0-10) using a simple formula"""
        # Count sentences
        sentences = sent_tokenize(text)
        
        # Count words
        words = word_tokenize(text)
        
        # Count syllables (approximate)
        syllables = sum(self._count_syllables(word) for word in words)
        
        # Calculate Flesch-Kincaid Grade Level (simplified)
        if len(words) == 0 or len(sentences) == 0:
            return 5  # Default middle score
            
        fk_score = 0.39 * (len(words) / len(sentences)) + 11.8 * (syllables / len(words)) - 15.59
        
        # Convert to 0-10 scale, where lower grade level means higher readability
        readability_score = max(0, min(10, 10 - (fk_score / 2)))
        
        return readability_score
    
    def _count_syllables(self, word):
        """Count syllables in a word (approximate)"""
        word = word.lower()
        if len(word) <= 3:
            return 1
            
        # Remove final e
        word = re.sub(r'e$', '', word)
        
        # Count vowel groups
        syllables = len(re.findall(r'[aeiouy]+', word))
        
        return max(1, syllables)
    
    def _analyze_structure(self, text):
        """Analyze document structure"""
        paragraphs = [p for p in text.split('\n\n') if p.strip()]
        
        if len(paragraphs) < 3:
            return "The document appears to have limited structure. Consider organizing content into clear introduction, body, and conclusion sections."
            
        # Analyze first paragraph (introduction)
        intro = paragraphs[0]
        intro_feedback = "Introduction: "
        if len(intro) < 200:
            intro_feedback += "Your introduction is concise, but may need more context. "
        elif len(intro) > 600:
            intro_feedback += "Your introduction is quite long. Consider making it more focused. "
        else:
            intro_feedback += "Your introduction has good length. "
            
        # Analyze middle paragraphs (body)
        body_feedback = "Body: "
        body_paragraphs = paragraphs[1:-1]
        avg_paragraph_len = sum(len(p) for p in body_paragraphs) / len(body_paragraphs) if body_paragraphs else 0
        
        if avg_paragraph_len < 300:
            body_feedback += "Your paragraphs are relatively short. Consider developing ideas more fully. "
        elif avg_paragraph_len > 800:
            body_feedback += "Your paragraphs are quite long. Consider breaking them into smaller, focused units. "
        else:
            body_feedback += "Your paragraph length is good. "
            
        # Analyze last paragraph (conclusion)
        conclusion = paragraphs[-1]
        conclusion_feedback = "Conclusion: "
        if len(conclusion) < 150:
            conclusion_feedback += "Your conclusion is brief. Consider summarizing key points more thoroughly. "
        elif len(conclusion) > 500:
            conclusion_feedback += "Your conclusion is quite long. Consider making it more concise. "
        else:
            conclusion_feedback += "Your conclusion has good length. "
            
        return intro_feedback + body_feedback + conclusion_feedback
    
    def _generate_improvement_suggestions(self, text, sentences, words, pos_tags):
        """Generate content improvement suggestions"""
        suggestions = []
        
        # Check for weak verbs
        weak_verbs = ['is', 'was', 'are', 'were', 'be', 'been', 'being', 'has', 'have', 'had']
        for i, (word, tag) in enumerate(pos_tags):
            if word.lower() in weak_verbs and tag.startswith('VB'):
                # Find the sentence containing this word
                for sent in sentences:
                    if word in sent.split():
                        suggestions.append({
                            'type': 'weak-verb',
                            'text': sent,
                            'position': {'start': text.find(sent), 'end': text.find(sent) + len(sent)},
                            'suggestion': 'Consider using a stronger, more specific verb'
                        })
                        break
        
        # Check for passive voice (simplified detection)
        for sent in sentences:
            sent_words = word_tokenize(sent)
            sent_tags = pos_tag(sent_words)
            
            # Look for 'be' verb followed by past participle
            for i in range(len(sent_tags) - 1):
                if sent_tags[i][0].lower() in ['is', 'are', 'was', 'were', 'be', 'been', 'being'] and \
                   sent_tags[i+1][1] == 'VBN':
                    suggestions.append({
                        'type': 'passive-voice',
                        'text': sent,
                        'position': {'start': text.find(sent), 'end': text.find(sent) + len(sent)},
                        'suggestion': 'Consider using active voice for more direct expression'
                    })
                    break
        
        # Check for excessive adverbs
        adverb_count = sum(1 for word, tag in pos_tags if tag == 'RB')
        if adverb_count > len(words) * 0.05:  # More than 5% adverbs
            suggestions.append({
                'type': 'excessive-adverbs',
                'text': None,
                'position': None,
                'suggestion': 'Your writing contains many adverbs. Consider replacing some with stronger verbs or more specific descriptions'
            })
            
        return suggestions
    
    def _generate_rewrite_suggestions(self, sentences):
        """Generate rewrite suggestions for problematic sections"""
        rewrite_suggestions = []
        
        # Find sentences with potential issues
        for sent in sentences:
            sent_words = word_tokenize(sent)
            
            # Check for very long sentences
            if len(sent_words) > 35:
                simpler_alternative = self._generate_simpler_alternative(sent)
                rewrite_suggestions.append({
                    'original': sent,
                    'suggestion': simpler_alternative,
                    'reason': 'Sentence is too long and may be difficult to follow'
                })
                
            # Check for sentences with many conjunctions
            conjunction_count = sent.lower().count(' and ') + sent.lower().count(' but ') + sent.lower().count(' or ')
            if conjunction_count >= 3:
                simpler_alternative = self._generate_simpler_alternative(sent)
                rewrite_suggestions.append({
                    'original': sent,
                    'suggestion': simpler_alternative,
                    'reason': 'Sentence contains many conjunctions and could be broken down'
                })
                
        return rewrite_suggestions
        
    def _generate_simpler_alternative(self, sentence):
        """Generate a simpler alternative for a complex sentence"""
        # This is a simplified implementation and would be more sophisticated in a real app
        
        # Break sentence at conjunctions
        parts = re.split(r' (?:and|but|or) ', sentence)
        
        if len(parts) > 1:
            # Simple splitting at conjunctions
            return '. '.join(part[0].upper() + part[1:] if i > 0 else part 
                            for i, part in enumerate(parts))
        
        # If we can't split at conjunctions, just break a long sentence in half
        words = word_tokenize(sentence)
        if len(words) > 20:
            mid = len(words) // 2
            
            # Find a natural break point near the middle (e.g., after a comma)
            for i in range(mid, mid - 10, -1):
                if i > 0 and words[i-1].endswith(','):
                    mid = i
                    break
            
            first_half = ' '.join(words[:mid])
            second_half = ' '.join(words[mid:])
            second_half = second_half[0].upper() + second_half[1:] if second_half else ""
            
            return first_half + '. ' + second_half
        
        return sentence 
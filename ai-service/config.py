"""
DeepAgent ATS System - Configuration
-------------------------------------
Central configuration for the advanced ATS system
"""

import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    """Configuration class for ATS system"""
    
    # MongoDB Configuration
    MONGODB_URI = os.getenv("MONGODB_URI")
    DATABASE_NAME = "SmartHireDB"
    JOBS_COLLECTION = "jobs"
    APPLICATIONS_COLLECTION = "job_applications"
    EMBEDDINGS_COLLECTION = "resume_embeddings"
    SKILLS_COLLECTION = "extracted_skills"
    
    # Gemini Configuration
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
    GEMINI_MODEL = "gemini-2.0-flash"  # Latest Gemini Flash 2.0
    
    # Embedding Configuration
    EMBEDDING_MODEL = "sentence-transformers/all-MiniLM-L6-v2"
    EMBEDDING_DIMENSION = 384
    
    # Vector Search Configuration
    VECTOR_INDEX_NAME = "resume_vector_index"
    SIMILARITY_THRESHOLD = 0.7
    TOP_K_RESULTS = 5
    
    # RAG Configuration
    CHUNK_SIZE = 512
    CHUNK_OVERLAP = 50
    MAX_CONTEXT_LENGTH = 4000
    
    # Agent Configuration
    AGENT_TIMEOUT = 30  # seconds
    MAX_RETRIES = 3
    
    # Scoring Weights
    WEIGHTS = {
        "semantic_similarity": 0.30,
        "skill_match": 0.25,
        "experience_match": 0.20,
        "education_match": 0.15,
        "llm_evaluation": 0.10
    }
    
    # Skill Categories
    SKILL_CATEGORIES = {
        "programming": ["python", "java", "javascript", "typescript", "c++", "go", "rust"],
        "web": ["react", "angular", "vue", "node.js", "express", "django", "flask", "fastapi"],
        "database": ["mongodb", "postgresql", "mysql", "redis", "elasticsearch"],
        "cloud": ["aws", "azure", "gcp", "docker", "kubernetes"],
        "ml_ai": ["machine learning", "deep learning", "nlp", "computer vision", "tensorflow", "pytorch"],
        "tools": ["git", "jenkins", "ci/cd", "agile", "scrum"]
    }
    
    @classmethod
    def validate(cls):
        """Validate required configuration"""
        if not cls.MONGODB_URI:
            raise ValueError("MONGODB_URI not found in environment variables")
        if not cls.GEMINI_API_KEY:
            raise ValueError("GEMINI_API_KEY not found in environment variables")
        return True

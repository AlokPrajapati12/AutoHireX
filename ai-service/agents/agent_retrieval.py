"""
Agent 1: Document Retrieval & Chunking Agent
---------------------------------------------
Retrieves documents from MongoDB and chunks them for processing
"""

import io
import PyPDF2
from typing import List, Dict, Optional
from pymongo import MongoClient
from bson import ObjectId, DBRef
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config


class DocumentRetrievalAgent:
    """Agent responsible for retrieving and chunking documents"""
    
    def __init__(self):
        self.client = MongoClient(Config.MONGODB_URI)
        self.db = self.client[Config.DATABASE_NAME]
        self.jobs = self.db[Config.JOBS_COLLECTION]
        self.applications = self.db[Config.APPLICATIONS_COLLECTION]
        
        print("✓ DocumentRetrievalAgent initialized")
    
    def extract_text_from_pdf(self, pdf_binary: bytes) -> str:
        """Extract text from PDF binary data"""
        try:
            pdf_file = io.BytesIO(pdf_binary)
            pdf_reader = PyPDF2.PdfReader(pdf_file)
            
            text = ""
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
            
            return text.strip()
        except Exception as e:
            print(f"❌ PDF extraction error: {e}")
            return ""
    
    def chunk_text(self, text: str, chunk_size: int = None, overlap: int = None) -> List[Dict[str, any]]:
        """
        Chunk text into smaller pieces with overlap
        Returns list of chunks with metadata
        """
        chunk_size = chunk_size or Config.CHUNK_SIZE
        overlap = overlap or Config.CHUNK_OVERLAP
        
        chunks = []
        words = text.split()
        
        for i in range(0, len(words), chunk_size - overlap):
            chunk_words = words[i:i + chunk_size]
            chunk_text = " ".join(chunk_words)
            
            chunks.append({
                "text": chunk_text,
                "chunk_id": len(chunks),
                "start_word": i,
                "end_word": i + len(chunk_words),
                "word_count": len(chunk_words)
            })
        
        return chunks
    
    def get_job_from_ref(self, job_ref) -> Optional[Dict]:
        """Resolve job from DBRef or ObjectId"""
        try:
            if isinstance(job_ref, DBRef):
                job_id = job_ref.id
            elif isinstance(job_ref, dict):
                if '$oid' in job_ref:
                    job_id = ObjectId(job_ref['$oid'])
                elif '$id' in job_ref:
                    job_id = job_ref['$id']
                    if isinstance(job_id, dict) and '$oid' in job_id:
                        job_id = ObjectId(job_id['$oid'])
                else:
                    job_id = ObjectId(job_ref)
            elif isinstance(job_ref, ObjectId):
                job_id = job_ref
            else:
                job_id = ObjectId(str(job_ref))
            
            return self.jobs.find_one({"_id": job_id})
        except Exception as e:
            print(f"❌ Job resolution error: {e}")
            return None
    
    def retrieve_application_data(self, application_id: ObjectId) -> Dict:
        """
        Retrieve application and associated job
        Returns structured data with chunks
        """
        # Get application
        application = self.applications.find_one({"_id": application_id})
        if not application:
            raise ValueError(f"Application not found: {application_id}")
        
        # Get job
        job = self.get_job_from_ref(application.get('job'))
        if not job:
            raise ValueError(f"Job not found for application: {application_id}")
        
        # Extract resume text
        resume_data = application.get('resumeData')
        if not resume_data:
            raise ValueError("No resume data in application")
        
        resume_text = self.extract_text_from_pdf(resume_data)
        if not resume_text:
            raise ValueError("Could not extract text from resume")
        
        # Chunk resume
        resume_chunks = self.chunk_text(resume_text)
        
        # Chunk job description
        jd_text = job.get('description', '')
        jd_chunks = self.chunk_text(jd_text) if jd_text else []
        
        # Prepare structured data
        data = {
            "application_id": application_id,
            "candidate_name": application.get('candidateName', 'Unknown'),
            "candidate_email": application.get('candidateEmail', ''),
            "job_id": job['_id'],
            "job_title": job.get('title', 'Unknown'),
            "company": job.get('company', 'Unknown'),
            "resume": {
                "full_text": resume_text,
                "chunks": resume_chunks,
                "total_chunks": len(resume_chunks),
                "word_count": len(resume_text.split())
            },
            "job_description": {
                "full_text": jd_text,
                "chunks": jd_chunks,
                "total_chunks": len(jd_chunks),
                "word_count": len(jd_text.split()) if jd_text else 0
            }
        }
        
        print(f"✓ Retrieved: {data['candidate_name']} → {data['job_title']}")
        print(f"  Resume: {data['resume']['word_count']} words, {data['resume']['total_chunks']} chunks")
        print(f"  JD: {data['job_description']['word_count']} words, {data['job_description']['total_chunks']} chunks")
        
        return data
    
    def retrieve_all_applications(self) -> List[Dict]:
        """Retrieve all applications for batch processing"""
        applications = list(self.applications.find())
        
        processed_data = []
        for app in applications:
            try:
                data = self.retrieve_application_data(app['_id'])
                processed_data.append(data)
            except Exception as e:
                print(f"⚠️  Skipped {app.get('candidateName', 'Unknown')}: {e}")
        
        return processed_data

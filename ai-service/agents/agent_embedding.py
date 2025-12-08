"""
Agent 2: Embedding & Vector Search Agent
-----------------------------------------
Generates embeddings and performs vector search
"""

import numpy as np
from typing import List, Dict
from sentence_transformers import SentenceTransformer
from pymongo import MongoClient
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config


class EmbeddingAgent:
    """Agent responsible for generating and searching embeddings"""
    
    def __init__(self):
        self.client = MongoClient(Config.MONGODB_URI)
        self.db = self.client[Config.DATABASE_NAME]
        self.embeddings_collection = self.db[Config.EMBEDDINGS_COLLECTION]
        
        # Load embedding model
        print(f"→ Loading embedding model: {Config.EMBEDDING_MODEL}")
        self.model = SentenceTransformer(Config.EMBEDDING_MODEL)
        print("✓ EmbeddingAgent initialized")
    
    def generate_embedding(self, text: str) -> np.ndarray:
        """Generate embedding for text"""
        embedding = self.model.encode(text, convert_to_numpy=True)
        return embedding
    
    def generate_chunk_embeddings(self, chunks: List[Dict]) -> List[Dict]:
        """Generate embeddings for all chunks"""
        texts = [chunk['text'] for chunk in chunks]
        embeddings = self.model.encode(texts, convert_to_numpy=True, show_progress_bar=False)
        
        for i, chunk in enumerate(chunks):
            chunk['embedding'] = embeddings[i].tolist()
        
        return chunks
    
    def store_resume_embedding(self, application_id, resume_chunks: List[Dict], metadata: Dict):
        """Store resume embeddings in MongoDB"""
        
        # Generate embeddings for chunks
        chunks_with_embeddings = self.generate_chunk_embeddings(resume_chunks)
        
        # Create full resume embedding (average of chunk embeddings)
        chunk_embeddings = [chunk['embedding'] for chunk in chunks_with_embeddings]
        full_embedding = np.mean(chunk_embeddings, axis=0).tolist()
        
        # Store in MongoDB
        document = {
            "application_id": application_id,
            "candidate_name": metadata['candidate_name'],
            "job_id": metadata['job_id'],
            "job_title": metadata['job_title'],
            "resume_embedding": full_embedding,
            "chunk_embeddings": chunks_with_embeddings,
            "metadata": metadata
        }
        
        # Upsert
        self.embeddings_collection.replace_one(
            {"application_id": application_id},
            document,
            upsert=True
        )
        
        print(f"✓ Stored embeddings for: {metadata['candidate_name']}")
        return full_embedding
    
    def cosine_similarity(self, vec1: np.ndarray, vec2: np.ndarray) -> float:
        """Calculate cosine similarity between two vectors"""
        dot_product = np.dot(vec1, vec2)
        norm1 = np.linalg.norm(vec1)
        norm2 = np.linalg.norm(vec2)
        
        if norm1 == 0 or norm2 == 0:
            return 0.0
        
        return dot_product / (norm1 * norm2)
    
    def semantic_search(self, query_embedding: np.ndarray, job_id=None, top_k: int = None) -> List[Dict]:
        """
        Perform semantic search using embeddings
        Note: This uses in-memory cosine similarity as fallback
        For production, use MongoDB Atlas Vector Search
        """
        top_k = top_k or Config.TOP_K_RESULTS
        
        # Build query
        query = {}
        if job_id:
            query['job_id'] = job_id
        
        # Fetch all embeddings
        documents = list(self.embeddings_collection.find(query))
        
        if not documents:
            return []
        
        # Calculate similarities
        results = []
        for doc in documents:
            resume_emb = np.array(doc['resume_embedding'])
            similarity = self.cosine_similarity(query_embedding, resume_emb)
            
            results.append({
                "application_id": doc['application_id'],
                "candidate_name": doc['candidate_name'],
                "job_title": doc['job_title'],
                "similarity_score": float(similarity),
                "metadata": doc.get('metadata', {})
            })
        
        # Sort by similarity
        results.sort(key=lambda x: x['similarity_score'], reverse=True)
        
        return results[:top_k]
    
    def compare_resume_with_jd(self, resume_chunks: List[Dict], jd_chunks: List[Dict]) -> Dict:
        """
        Compare resume with job description using semantic similarity
        Returns detailed similarity analysis
        """
        
        # Generate embeddings
        resume_chunks_emb = self.generate_chunk_embeddings(resume_chunks.copy())
        jd_chunks_emb = self.generate_chunk_embeddings(jd_chunks.copy())
        
        # Calculate chunk-level similarities
        chunk_similarities = []
        for r_chunk in resume_chunks_emb:
            r_emb = np.array(r_chunk['embedding'])
            
            for jd_chunk in jd_chunks_emb:
                jd_emb = np.array(jd_chunk['embedding'])
                similarity = self.cosine_similarity(r_emb, jd_emb)
                
                if similarity > Config.SIMILARITY_THRESHOLD:
                    chunk_similarities.append({
                        "resume_chunk_id": r_chunk['chunk_id'],
                        "jd_chunk_id": jd_chunk['chunk_id'],
                        "similarity": float(similarity),
                        "resume_text": r_chunk['text'][:100] + "...",
                        "jd_text": jd_chunk['text'][:100] + "..."
                    })
        
        # Calculate overall similarity
        resume_full_emb = np.mean([chunk['embedding'] for chunk in resume_chunks_emb], axis=0)
        jd_full_emb = np.mean([chunk['embedding'] for chunk in jd_chunks_emb], axis=0)
        overall_similarity = self.cosine_similarity(resume_full_emb, jd_full_emb)
        
        # Find best matching chunks
        chunk_similarities.sort(key=lambda x: x['similarity'], reverse=True)
        
        analysis = {
            "overall_similarity": float(overall_similarity),
            "overall_score": float(overall_similarity * 100),
            "high_similarity_chunks": len([s for s in chunk_similarities if s['similarity'] > 0.8]),
            "medium_similarity_chunks": len([s for s in chunk_similarities if 0.6 < s['similarity'] <= 0.8]),
            "top_matching_chunks": chunk_similarities[:5]
        }
        
        print(f"✓ Semantic similarity: {analysis['overall_score']:.2f}%")
        print(f"  High-match chunks: {analysis['high_similarity_chunks']}")
        
        return analysis

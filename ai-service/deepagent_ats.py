"""
DeepAgent ATS Orchestrator
---------------------------
Main orchestrator that coordinates all agents using MCP pattern.
Updated to fix DBRef issues, handle Job IDs correctly, pass Job Titles to embeddings, and sort results.
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from agents.agent_retrieval import DocumentRetrievalAgent
from agents.agent_embedding import EmbeddingAgent
from agents.agent_skill_extraction import SkillExtractionAgent
from agents.agent_llm_evaluation import LLMEvaluationAgent
from config import Config
from pymongo import MongoClient
from bson import ObjectId
from datetime import datetime
from typing import Dict, List
import json


class DeepAgentATSOrchestrator:
    """
    Main orchestrator for Deep Agent ATS System
    """
    
    def __init__(self):
        print("\n" + "="*70)
        print("🚀 INITIALIZING DEEPAGENT ATS SYSTEM")
        print("="*70)
        
        Config.validate()
        
        # Initialize MongoDB
        self.client = MongoClient(Config.MONGODB_URI)
        self.db = self.client[Config.DATABASE_NAME]
        
        # Connect to BOTH collections
        self.applications = self.db[Config.APPLICATIONS_COLLECTION] # job_applications
        self.jobs_collection = self.db['jobs']
        
        # Initialize agents
        print("\n→ Initializing Agents...")
        self.retrieval_agent = DocumentRetrievalAgent()
        self.embedding_agent = EmbeddingAgent()
        self.skill_agent = SkillExtractionAgent()
        self.llm_agent = LLMEvaluationAgent()
        
        print("\n✓ All agents initialized successfully!")
        print("="*70 + "\n")
    
    def calculate_final_score(self, scores: Dict) -> float:
        """Calculate weighted final ATS score"""
        final_score = (
            scores['semantic_similarity'] * Config.WEIGHTS['semantic_similarity'] +
            scores['skill_match'] * Config.WEIGHTS['skill_match'] +
            scores['experience_match'] * Config.WEIGHTS['experience_match'] +
            scores['education_match'] * Config.WEIGHTS['education_match'] +
            scores['llm_score'] * Config.WEIGHTS['llm_evaluation']
        )
        return round(final_score, 2)

    def fetch_active_jobs(self) -> List[Dict]:
        """
        Fetch ALL jobs directly from the 'jobs' collection
        and count how many applicants exist for each.
        """
        # 1. Get all jobs from the jobs collection
        active_jobs = list(self.jobs_collection.find({}))
        
        job_list = []
        
        # 2. For each job, count matching applications
        for job in active_jobs:
            job_id = job.get('_id')
            title = job.get('title', 'Untitled')
            company = job.get('company', 'Unknown Company')
            
            # FIXED: Uses "job.$id" to correctly count DBRef relationships
            app_count = self.applications.count_documents({"job.$id": job_id})
            
            job_list.append({
                "_id": job_id,
                "title": title,
                "company": company,
                "count": app_count
            })
            
        return job_list
    
    def process_all_applications(self):
        """Process ALL applications in the database regardless of job"""
        print("\n" + "="*70)
        print("🚀 BATCH PROCESSING: ALL APPLICATIONS")
        print("="*70 + "\n")

        # Fetch all applications
        applications = list(self.applications.find({}))

        if not applications:
            print("❌ No applications found in the database.")
            return

        results = []
        for i, app in enumerate(applications, 1):
            name = app.get('candidateName', 'Unknown Candidate')
            print(f"\n[{i}/{len(applications)}] Processing {name}...")
            
            try:
                result = self.process_single_application(app['_id'])
                if result:
                    results.append(result)
            except Exception as inner_e:
                print(f"  ❌ Skipped {name} due to error: {inner_e}")
        
        self._display_summary(results)

    def process_applications_by_job(self, job_id_str: str):
        """Process applications for the selected Job ID"""
        
        try:
            display_title = "Selected Job"
            
            # CASE 1: Handle Specific Job ID
            job_oid = ObjectId(job_id_str)
            
            # Fetch job details for display
            job_doc = self.jobs_collection.find_one({"_id": job_oid})
            if job_doc:
                display_title = f"{job_doc.get('title')} at {job_doc.get('company')}"
            
            # FIXED: Uses "job.$id" to match the DBRef field
            query = {"job.$id": job_oid}
            
            # Find applications
            applications = list(self.applications.find(query))
            
            print("\n" + "="*70)
            print(f"🚀 BATCH PROCESSING: {display_title}")
            print(f"ID: {job_id_str}")
            print(f"Found {len(applications)} applications")
            print("="*70 + "\n")
            
            if not applications:
                print("❌ No applications found for this specific job.")
                return

            results = []
            for i, app in enumerate(applications, 1):
                name = app.get('candidateName', 'Unknown Candidate')
                print(f"\n[{i}/{len(applications)}] Processing {name}...")
                
                try:
                    result = self.process_single_application(app['_id'])
                    if result:
                        results.append(result)
                except Exception as inner_e:
                    print(f"  ❌ Skipped {name} due to error: {inner_e}")
            
            self._display_summary(results)
            
        except Exception as e:
            print(f"❌ Error processing job batch: {e}")
    
    def process_single_application(self, application_id: ObjectId) -> Dict:
        """Process a single application"""
        
        print("\n" + "╔" + "="*68 + "╗")
        print(f"║  PROCESSING APPLICATION: {str(application_id):41} ║")
        print("╚" + "="*68 + "╝\n")
        
        try:
            # 1. Fetch the raw application doc first to get the Job ID
            app_doc = self.applications.find_one({"_id": application_id})
            job_ref = app_doc.get('job')
            job_id_str = str(job_ref.id) if job_ref else "unknown_job"

            # AGENT 1
            print("┌─ AGENT 1: Document Retrieval ────────────────────────────┐")
            data = self.retrieval_agent.retrieve_application_data(application_id)
            print("└──────────────────────────────────────────────────────────┘\n")
            
            # AGENT 2
            print("┌─ AGENT 2: Embedding & Semantic Search ───────────────────┐")
            semantic_analysis = self.embedding_agent.compare_resume_with_jd(
                data['resume']['chunks'],
                data['job_description']['chunks']
            )
            
            # ✅ FIXED: Added 'job_title' and 'company' to metadata to prevent KeyError
            metadata = {
                'candidate_name': data['candidate_name'], 
                'job_id': job_id_str,
                'job_title': data['job_title'],
                'company': data['company']
            }
            
            self.embedding_agent.store_resume_embedding(
                application_id, 
                data['resume']['chunks'], 
                metadata
            )
            print("└──────────────────────────────────────────────────────────┘\n")
            
            # AGENT 3
            print("┌─ AGENT 3: Skill Extraction & Analysis ───────────────────┐")
            skill_analysis = self.skill_agent.analyze_application(
                data['resume']['full_text'],
                data['job_description']['full_text']
            )
            self.skill_agent.store_skills(application_id, skill_analysis)
            print("└──────────────────────────────────────────────────────────┘\n")
            
            # AGENT 4
            print("┌─ AGENT 4: LLM Evaluation (Gemini + RAG) ─────────────────┐")
            context = self.llm_agent.build_context(data, semantic_analysis, skill_analysis)
            llm_evaluation = self.llm_agent.generate_ats_evaluation(
                context,
                data['resume']['full_text'],
                data['job_description']['full_text']
            )
            print("└──────────────────────────────────────────────────────────┘\n")
            
            # Scores
            scores = {
                'semantic_similarity': semantic_analysis['overall_score'],
                'skill_match': skill_analysis['comparison']['skills']['match_percentage'],
                'experience_match': 100 if skill_analysis['comparison']['experience_match'] else 50,
                'education_match': 75 if skill_analysis['resume']['education']['degrees'] else 50,
                'llm_score': llm_evaluation['ats_score'] if llm_evaluation else 50
            }
            
            final_score = self.calculate_final_score(scores)
            
            results = {
                "application_id": str(application_id),
                "candidate_name": data['candidate_name'],
                "job_title": data['job_title'],
                "company": data['company'],
                "final_ats_score": final_score,
                "component_scores": scores,
                "semantic_analysis": {"overall_score": semantic_analysis['overall_score']},
                "skill_analysis": {"match_percentage": skill_analysis['comparison']['skills']['match_percentage'], "matching_skills": skill_analysis['comparison']['skills']['matching_skills']},
                "llm_evaluation": llm_evaluation,
                "timestamp": datetime.utcnow()
            }
            
            self._display_results(results)
            self._update_mongodb(application_id, results)
            return results
            
        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()
            return None

    def _display_results(self, results: Dict):
        print(f"\n>>> FINAL SCORE: {results['final_ats_score']:.2f}% ({results['candidate_name']})")

    def _update_mongodb(self, application_id: ObjectId, results: Dict):
        update_data = {
            "atsScore": results['final_ats_score'],
            "atsScoreBreakdown": results['component_scores'],
            "atsEvaluation": results['llm_evaluation'],
            "skillAnalysis": results['skill_analysis'],
            "atsUpdatedAt": datetime.utcnow()
        }
        self.applications.update_one({"_id": application_id}, {"$set": update_data})
        print("✓ MongoDB updated")
    
    def _display_summary(self, results: List[Dict]):
        print("\n" + "="*70)
        print("📊 PROCESSING SUMMARY (Ranked by Score: Top Candidate First)")
        print("="*70)
        if not results:
            print("No applications processed.")
            return
            
        # SORTING LOGIC: Reverse=True means Highest Score First (Top to Bottom)
        results.sort(key=lambda x: x['final_ats_score'], reverse=True)
        
        print(f"{'Rank':<6} {'Candidate':<25} {'Score':>8}")
        print("─"*40)
        for i, r in enumerate(results, 1):
            print(f"{i:<6} {r['candidate_name'][:24]:<25} {r['final_ats_score']:>7.2f}%")
        print("\n")


def main():
    orchestrator = DeepAgentATSOrchestrator()
    
    while True:
        print("\nSELECT MODE:")
        print("─"*70)
        print("1. Process ALL applications")
        print("2. Select Job Role (Frontend, Backend, etc.)")
        print("3. Process by ID")
        print("q. Quit")
        print("─"*70)
        
        choice = input("\nEnter choice: ").strip()
        
        if choice == "1":
            orchestrator.process_all_applications()
        
        elif choice == "2":
            print("\nFetching Jobs from 'jobs' collection...")
            jobs = orchestrator.fetch_active_jobs()
            
            if not jobs:
                print("❌ No jobs found in 'jobs' collection.")
                continue

            print(f"\n{'No.':<4} {'Job Title':<30} {'Company':<15} {'Applicants'}")
            print("─"*70)
            
            # Helper to map selection number to Job ID
            job_map = {}
            
            for idx, job in enumerate(jobs, 1):
                j_id = str(job['_id'])
                job_map[idx] = j_id
                
                print(f"{idx:<4} {job['title']:<30} {job['company']:<15} {job['count']}")
                
            print("─"*70)
            
            selection = input(f"\nSelect Job Number (1-{len(jobs)}): ").strip()
            
            if selection.isdigit() and int(selection) in job_map:
                selected_job_id = job_map[int(selection)]
                orchestrator.process_applications_by_job(selected_job_id)
            else:
                print("❌ Invalid selection.")

        elif choice == "3":
            app_id = input("Enter Application ID: ").strip()
            try:
                orchestrator.process_single_application(ObjectId(app_id))
            except Exception as e:
                print(f"❌ Invalid ID: {e}")
        
        elif choice.lower() == 'q':
            break

if __name__ == "__main__":
    main()
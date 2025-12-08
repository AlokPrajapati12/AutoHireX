"""
Agent 3: Skill Extraction Agent
--------------------------------
Extracts skills, experience, and qualifications using NLP
"""

import re
from typing import List, Dict, Set
from collections import Counter
from pymongo import MongoClient
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config


class SkillExtractionAgent:
    """Agent responsible for extracting skills and experience"""
    
    def __init__(self):
        self.client = MongoClient(Config.MONGODB_URI)
        self.db = self.client[Config.DATABASE_NAME]
        self.skills_collection = self.db[Config.SKILLS_COLLECTION]
        
        # Build comprehensive skill database
        self.all_skills = self._build_skill_database()
        
        print("✓ SkillExtractionAgent initialized")
        print(f"  Total skills in database: {len(self.all_skills)}")
    
    def _build_skill_database(self) -> Set[str]:
        """Build comprehensive skill database from config"""
        skills = set()
        for category_skills in Config.SKILL_CATEGORIES.values():
            skills.update([s.lower() for s in category_skills])
        
        # Add common variations
        additional_skills = [
            # Programming languages
            "c#", "php", "ruby", "swift", "kotlin", "scala", "r", "matlab",
            # Frontend
            "html", "css", "sass", "less", "webpack", "vite", "next.js", "nuxt.js",
            # Backend
            "spring boot", "laravel", "ruby on rails", "asp.net",
            # Mobile
            "react native", "flutter", "ios", "android", "xamarin",
            # DevOps
            "terraform", "ansible", "puppet", "chef", "gitlab ci", "github actions",
            # Testing
            "jest", "pytest", "selenium", "cypress", "junit", "mocha",
            # Data
            "pandas", "numpy", "scipy", "matplotlib", "seaborn", "tableau", "power bi",
            # Big Data
            "hadoop", "spark", "kafka", "airflow", "databricks",
            # Others
            "rest api", "graphql", "microservices", "oauth", "jwt", "websockets"
        ]
        skills.update(additional_skills)
        
        return skills
    
    def extract_skills_from_text(self, text: str) -> Dict:
        """Extract skills from text"""
        text_lower = text.lower()
        
        # Find skills
        found_skills = []
        for skill in self.all_skills:
            # Use word boundaries for exact matches
            pattern = r'\b' + re.escape(skill) + r'\b'
            if re.search(pattern, text_lower):
                found_skills.append(skill)
        
        # Categorize skills
        categorized_skills = {category: [] for category in Config.SKILL_CATEGORIES.keys()}
        categorized_skills["other"] = []
        
        for skill in found_skills:
            categorized = False
            for category, category_skills in Config.SKILL_CATEGORIES.items():
                if skill in [s.lower() for s in category_skills]:
                    categorized_skills[category].append(skill)
                    categorized = True
                    break
            
            if not categorized:
                categorized_skills["other"].append(skill)
        
        return {
            "total_skills": len(found_skills),
            "skills": found_skills,
            "categorized_skills": categorized_skills,
            "skill_frequency": dict(Counter(found_skills))
        }
    
    def extract_experience(self, text: str) -> Dict:
        """Extract years of experience mentioned in text"""
        patterns = [
            r'(\d+)\+?\s*(?:years?|yrs?)\s*(?:of)?\s*(?:experience|exp)',
            r'experience.*?(\d+)\+?\s*(?:years?|yrs?)',
            r'(\d+)\+?\s*(?:years?|yrs?)',
        ]
        
        experiences = []
        for pattern in patterns:
            matches = re.findall(pattern, text.lower())
            experiences.extend([int(m) for m in matches])
        
        return {
            "mentioned_experiences": experiences,
            "max_experience": max(experiences) if experiences else 0,
            "min_experience": min(experiences) if experiences else 0,
            "avg_experience": sum(experiences) / len(experiences) if experiences else 0
        }
    
    def extract_education(self, text: str) -> Dict:
        """Extract education qualifications"""
        degrees = {
            "phd": ["phd", "ph.d", "doctorate", "doctoral"],
            "masters": ["master", "m.s", "m.sc", "mca", "mba", "m.tech", "m.e"],
            "bachelors": ["bachelor", "b.s", "b.sc", "bca", "b.tech", "b.e", "bba"],
            "diploma": ["diploma", "associate"]
        }
        
        text_lower = text.lower()
        found_degrees = []
        
        for degree_level, keywords in degrees.items():
            for keyword in keywords:
                if keyword in text_lower:
                    found_degrees.append(degree_level)
                    break
        
        return {
            "degrees": list(set(found_degrees)),
            "highest_degree": found_degrees[0] if found_degrees else None
        }
    
    def compare_skills(self, resume_skills: List[str], jd_skills: List[str]) -> Dict:
        """Compare resume skills with job description skills"""
        resume_set = set(resume_skills)
        jd_set = set(jd_skills)
        
        matching_skills = resume_set.intersection(jd_set)
        missing_skills = jd_set - resume_set
        extra_skills = resume_set - jd_set
        
        match_percentage = (len(matching_skills) / len(jd_set) * 100) if jd_set else 0
        
        return {
            "matching_skills": list(matching_skills),
            "missing_skills": list(missing_skills),
            "extra_skills": list(extra_skills),
            "match_count": len(matching_skills),
            "required_count": len(jd_set),
            "match_percentage": match_percentage
        }
    
    def analyze_application(self, resume_text: str, jd_text: str) -> Dict:
        """Comprehensive analysis of resume vs job description"""
        
        # Extract from resume
        resume_skills = self.extract_skills_from_text(resume_text)
        resume_experience = self.extract_experience(resume_text)
        resume_education = self.extract_education(resume_text)
        
        # Extract from JD
        jd_skills = self.extract_skills_from_text(jd_text)
        jd_experience = self.extract_experience(jd_text)
        jd_education = self.extract_education(jd_text)
        
        # Compare skills
        skill_comparison = self.compare_skills(
            resume_skills['skills'],
            jd_skills['skills']
        )
        
        # Experience match
        experience_match = False
        if jd_experience['min_experience'] > 0:
            experience_match = resume_experience['max_experience'] >= jd_experience['min_experience']
        
        analysis = {
            "resume": {
                "skills": resume_skills,
                "experience": resume_experience,
                "education": resume_education
            },
            "job_description": {
                "skills": jd_skills,
                "experience": jd_experience,
                "education": jd_education
            },
            "comparison": {
                "skills": skill_comparison,
                "experience_match": experience_match,
                "skill_match_score": skill_comparison['match_percentage']
            }
        }
        
        print(f"✓ Skills extracted:")
        print(f"  Resume: {resume_skills['total_skills']} skills")
        print(f"  JD: {jd_skills['total_skills']} skills")
        print(f"  Matching: {skill_comparison['match_count']} ({skill_comparison['match_percentage']:.1f}%)")
        
        return analysis
    
    def store_skills(self, application_id, skills_data: Dict):
        """Store extracted skills in MongoDB"""
        document = {
            "application_id": application_id,
            "skills_data": skills_data,
            "candidate_name": skills_data.get('candidate_name', 'Unknown')
        }
        
        self.skills_collection.replace_one(
            {"application_id": application_id},
            document,
            upsert=True
        )
        
        print(f"✓ Stored skills data for application: {application_id}")

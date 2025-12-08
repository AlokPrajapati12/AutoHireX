"""
Agent 4: LLM Evaluation Agent (Gemini Flash 2.0)
-------------------------------------------------
Uses Google Gemini for intelligent ATS evaluation with RAG
"""

import google.generativeai as genai
from typing import Dict, List
import json
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config


class LLMEvaluationAgent:
    """Agent responsible for LLM-based evaluation using Gemini"""
    
    def __init__(self):
        # Configure Gemini
        genai.configure(api_key=Config.GEMINI_API_KEY)
        
        # Initialize model
        self.model = genai.GenerativeModel(Config.GEMINI_MODEL)
        
        print(f"✓ LLMEvaluationAgent initialized with {Config.GEMINI_MODEL}")
    
    def build_context(self, data: Dict, semantic_analysis: Dict, skill_analysis: Dict) -> str:
        """Build context for RAG using retrieved information"""
        
        # Extract key information
        candidate = data['candidate_name']
        job_title = data['job_title']
        company = data['company']
        
        # Semantic similarity info
        semantic_score = semantic_analysis['overall_score']
        top_chunks = semantic_analysis['top_matching_chunks'][:3]
        
        # Skill info
        skills = skill_analysis['comparison']['skills']
        matching_skills = skills['matching_skills']
        missing_skills = skills['missing_skills']
        skill_match_pct = skills['match_percentage']
        
        # Build context string
        context = f"""
# ATS EVALUATION CONTEXT

## Candidate Information
- Name: {candidate}
- Applying for: {job_title} at {company}

## Semantic Similarity Analysis
- Overall Similarity Score: {semantic_score:.2f}%
- Analysis: The resume content shows {'strong' if semantic_score > 70 else 'moderate' if semantic_score > 50 else 'weak'} semantic alignment with job requirements.

### Top Matching Resume Sections:
"""
        
        for i, chunk in enumerate(top_chunks, 1):
            context += f"{i}. Similarity: {chunk['similarity']*100:.1f}%\n"
            context += f"   Resume: {chunk['resume_text']}\n"
            context += f"   JD Match: {chunk['jd_text']}\n\n"
        
        context += f"""
## Skills Analysis
- Skills Match: {skill_match_pct:.1f}% ({len(matching_skills)}/{len(matching_skills) + len(missing_skills)} required skills)

### Matching Skills:
{', '.join(matching_skills[:20]) if matching_skills else 'None'}

### Missing Critical Skills:
{', '.join(missing_skills[:15]) if missing_skills else 'None'}

## Experience & Education
- Resume Experience: {skill_analysis['resume']['experience']['max_experience']} years mentioned
- Required Experience: {skill_analysis['job_description']['experience']['min_experience']} years
- Resume Education: {', '.join(skill_analysis['resume']['education']['degrees']) if skill_analysis['resume']['education']['degrees'] else 'Not clearly specified'}
"""
        
        return context
    
    def generate_ats_evaluation(self, context: str, resume_text: str, jd_text: str) -> Dict:
        """
        Generate comprehensive ATS evaluation using Gemini with RAG context
        """
        
        # Limit text length for API
        resume_preview = resume_text[:Config.MAX_CONTEXT_LENGTH]
        jd_preview = jd_text[:Config.MAX_CONTEXT_LENGTH]
        
        prompt = f"""
You are an expert ATS (Applicant Tracking System) evaluator and HR professional. You have been provided with comprehensive analysis data about a job application.

{context}

## Resume Preview:
{resume_preview}

## Job Description Preview:
{jd_preview}

---

Based on the provided context, semantic analysis, and skill matching data, provide a comprehensive ATS evaluation with the following structure:

1. **Overall ATS Score** (0-100): Provide a numerical score
2. **Key Strengths**: List 3-5 main strengths of this application
3. **Key Weaknesses**: List 3-5 main weaknesses or gaps
4. **Detailed Analysis**: 
   - Skills alignment evaluation
   - Experience relevance
   - Cultural and role fit indicators
5. **Recommendations**: 
   - For the candidate: How to improve their resume
   - For the recruiter: Interview focus areas
6. **Pass/Fail Decision**: PASS or FAIL with justification
7. **Interview Recommendation**: YES or NO with confidence level (HIGH/MEDIUM/LOW)

Respond in JSON format with these exact keys:
{{
  "ats_score": <number 0-100>,
  "overall_assessment": "<brief summary>",
  "strengths": ["<strength1>", "<strength2>", ...],
  "weaknesses": ["<weakness1>", "<weakness2>", ...],
  "detailed_analysis": {{
    "skills_alignment": "<analysis>",
    "experience_relevance": "<analysis>",
    "role_fit": "<analysis>"
  }},
  "recommendations": {{
    "for_candidate": ["<rec1>", "<rec2>", ...],
    "for_recruiter": ["<rec1>", "<rec2>", ...]
  }},
  "decision": "PASS" or "FAIL",
  "decision_justification": "<explanation>",
  "interview_recommendation": "YES" or "NO",
  "interview_confidence": "HIGH" or "MEDIUM" or "LOW",
  "interview_focus_areas": ["<area1>", "<area2>", ...]
}}

Be objective, professional, and data-driven in your evaluation. Consider the semantic similarity scores and skill matches provided in the context.
"""
        
        try:
            print("→ Generating LLM evaluation with Gemini...")
            
            # Generate response
            response = self.model.generate_content(prompt)
            response_text = response.text
            
            # Extract JSON from response
            # Handle markdown code blocks
            if "```json" in response_text:
                response_text = response_text.split("```json")[1].split("```")[0].strip()
            elif "```" in response_text:
                response_text = response_text.split("```")[1].split("```")[0].strip()
            
            # Parse JSON
            evaluation = json.loads(response_text)
            
            print(f"✓ LLM Evaluation complete")
            print(f"  ATS Score: {evaluation['ats_score']}/100")
            print(f"  Decision: {evaluation['decision']}")
            print(f"  Interview: {evaluation['interview_recommendation']} ({evaluation['interview_confidence']})")
            
            return evaluation
            
        except json.JSONDecodeError as e:
            print(f"⚠️  JSON parsing error: {e}")
            print(f"  Raw response: {response_text[:500]}")
            
            # Fallback evaluation
            return {
                "ats_score": 50,
                "overall_assessment": "Evaluation failed - using fallback",
                "strengths": ["Could not parse LLM response"],
                "weaknesses": ["Evaluation error"],
                "detailed_analysis": {
                    "skills_alignment": "Error in evaluation",
                    "experience_relevance": "Error in evaluation",
                    "role_fit": "Error in evaluation"
                },
                "recommendations": {
                    "for_candidate": ["Retry evaluation"],
                    "for_recruiter": ["Retry evaluation"]
                },
                "decision": "FAIL",
                "decision_justification": "Evaluation error",
                "interview_recommendation": "NO",
                "interview_confidence": "LOW",
                "interview_focus_areas": []
            }
        
        except Exception as e:
            print(f"❌ LLM evaluation error: {e}")
            return None
    
    def generate_feedback_report(self, evaluation: Dict, candidate_name: str, job_title: str) -> str:
        """Generate human-readable feedback report"""
        
        report = f"""
╔══════════════════════════════════════════════════════════════╗
║              ATS EVALUATION REPORT                           ║
╚══════════════════════════════════════════════════════════════╝

Candidate: {candidate_name}
Position: {job_title}
ATS Score: {evaluation['ats_score']}/100
Decision: {evaluation['decision']}

───────────────────────────────────────────────────────────────
OVERALL ASSESSMENT
───────────────────────────────────────────────────────────────
{evaluation['overall_assessment']}

───────────────────────────────────────────────────────────────
KEY STRENGTHS
───────────────────────────────────────────────────────────────
"""
        
        for i, strength in enumerate(evaluation['strengths'], 1):
            report += f"{i}. {strength}\n"
        
        report += f"""
───────────────────────────────────────────────────────────────
KEY WEAKNESSES
───────────────────────────────────────────────────────────────
"""
        
        for i, weakness in enumerate(evaluation['weaknesses'], 1):
            report += f"{i}. {weakness}\n"
        
        report += f"""
───────────────────────────────────────────────────────────────
DETAILED ANALYSIS
───────────────────────────────────────────────────────────────
Skills Alignment: {evaluation['detailed_analysis']['skills_alignment']}

Experience Relevance: {evaluation['detailed_analysis']['experience_relevance']}

Role Fit: {evaluation['detailed_analysis']['role_fit']}

───────────────────────────────────────────────────────────────
RECOMMENDATIONS
───────────────────────────────────────────────────────────────
For Candidate:
"""
        
        for i, rec in enumerate(evaluation['recommendations']['for_candidate'], 1):
            report += f"{i}. {rec}\n"
        
        report += f"""
For Recruiter:
"""
        
        for i, rec in enumerate(evaluation['recommendations']['for_recruiter'], 1):
            report += f"{i}. {rec}\n"
        
        report += f"""
───────────────────────────────────────────────────────────────
INTERVIEW RECOMMENDATION
───────────────────────────────────────────────────────────────
Recommendation: {evaluation['interview_recommendation']}
Confidence: {evaluation['interview_confidence']}
Decision Justification: {evaluation['decision_justification']}

Focus Areas for Interview:
"""
        
        for i, area in enumerate(evaluation['interview_focus_areas'], 1):
            report += f"{i}. {area}\n"
        
        report += "\n═══════════════════════════════════════════════════════════════\n"
        
        return report

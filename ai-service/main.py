#!/usr/bin/env python3
"""
main.py — Smart Hire AI (LangGraph) orchestrator (fixed)

Notes:
- Sets a project-local model cache directory (creates ./model_cache) to avoid permission errors.
- Ensures the LangGraph workflow has an explicit terminal (END) edge so it won't be a dead-end.
- Preserves your FastAPI endpoints and agent orchestration.
"""

from __future__ import annotations
import os
import sys
from datetime import datetime
from typing import TypedDict, List, Dict, Any, Optional

# -------------------------
# MODEL CACHE (fix permission issues)
# -------------------------
# Create a local cache inside the project to avoid system-wide permission problems.
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CACHE_DIR = os.path.join(BASE_DIR, "model_cache")

# Prefer user-local caches if can't create project cache
try:
    os.makedirs(CACHE_DIR, exist_ok=True)
    os.environ["HF_HOME"] = CACHE_DIR
    os.environ["TRANSFORMERS_CACHE"] = CACHE_DIR
    os.environ["SENTENCE_TRANSFORMERS_HOME"] = CACHE_DIR
except Exception as exc:
    # fallback to user cache if project cache cannot be created
    fallback = os.path.expanduser("~/.cache/smarthire_models")
    os.makedirs(fallback, exist_ok=True)
    os.environ["HF_HOME"] = fallback
    os.environ["TRANSFORMERS_CACHE"] = fallback
    os.environ["SENTENCE_TRANSFORMERS_HOME"] = fallback
    print(f"⚠️ Warning: could not create project model cache at {CACHE_DIR}: {exc}")
    print(f"Using fallback cache at {fallback}")

# Put project directory into sys.path (so local imports work reliably)
sys.path.append(BASE_DIR)

# -------------------------
# Standard imports (after cache env is set)
# -------------------------
import requests
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
from pymongo import MongoClient
from pymongo.server_api import ServerApi
from bson import ObjectId

# LangGraph imports (START/END available in the langgraph API)
from langgraph.graph import StateGraph

# DeepAgent and config
from deepagent_ats import DeepAgentATSOrchestrator
from config import Config

# Agents / routers
from agents.schedule_agent import ScheduleAgent
from agents.voice_agent import voice_router
from agents.agent_jd_generator import jd_agent
from agents.offer_onboarding_agent import OfferOnboardingAgent

# Optional imports (non-fatal)
try:
    from langchain_google_genai import ChatGoogleGenerativeAI
    from langchain_core.messages import HumanMessage
    from langchain_community.utilities.tavily_search import TavilySearchAPIWrapper
except Exception:
    ChatGoogleGenerativeAI = None
    HumanMessage = None
    TavilySearchAPIWrapper = None

# -------------------------
# Load environment variables
# -------------------------
load_dotenv()
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
MONGODB_URI = os.getenv("MONGODB_URI")
WEBHOOK_URL = os.getenv("WEBHOOK_URL", "https://webhook.site/92b7a908-f6dc-41ca-8e56-84f082e9da5c")

# -------------------------
# MongoDB helpers
# -------------------------
def get_mongodb_connection():
    if not MONGODB_URI:
        return None
    try:
        client = MongoClient(MONGODB_URI, server_api=ServerApi("1"))
        return client
    except Exception as e:
        print(f"MongoDB connection error: {e}")
        return None

def get_candidate_email_from_mongodb(application_id: str) -> Optional[str]:
    client = get_mongodb_connection()
    if not client:
        return None
    try:
        db = client[Config.DATABASE_NAME]
        app_doc = db[Config.APPLICATIONS_COLLECTION].find_one({"_id": ObjectId(application_id)})
        return app_doc.get("candidateEmail") if app_doc else None
    except Exception as e:
        print(f"Error fetching email for {application_id}: {e}")
        return None
    finally:
        try:
            client.close()
        except Exception:
            pass

# -------------------------
# Global agent initialisation (safe)
# -------------------------
try:
    deep_ats = DeepAgentATSOrchestrator()
    print("✅ DeepAgent ATS Agents initialized for LangGraph")
except Exception as e:
    print(f"⚠️ Warning: DeepAgent ATS initialization failed: {e}")
    deep_ats = None

# schedule & offer agents (best-effort)
try:
    scheduler = ScheduleAgent()
except Exception as e:
    print(f"⚠️ Warning: ScheduleAgent init failed: {e}")
    scheduler = None

try:
    offer_onboarding_agent = OfferOnboardingAgent(
        get_email_helper_func=get_candidate_email_from_mongodb,
        config=Config
    )
    print("✓ OfferOnboardingAgent initialized")
except Exception as e:
    print(f"⚠️ Warning: OfferOnboardingAgent initialization failed: {e}")
    offer_onboarding_agent = None

# -------------------------
# LangGraph ATS workflow
# -------------------------
class ATSProcessingState(TypedDict):
    application_id: ObjectId
    data: Dict
    semantic_analysis: Dict
    skill_analysis: Dict
    llm_evaluation: Dict
    final_result: Dict
    error: Optional[str]

def node_retrieval(state: ATSProcessingState) -> ATSProcessingState:
    if deep_ats is None:
        return {"error": "ATS Agent not initialized."}
    try:
        print(f"--- Node: Retrieval ({state.get('application_id')}) ---")
        data = deep_ats.retrieval_agent.retrieve_application_data(state["application_id"])
        return {"data": data, "error": None}
    except Exception as e:
        return {"error": str(e)}

def node_embedding(state: ATSProcessingState) -> ATSProcessingState:
    if state.get("error"):
        return state
    if deep_ats is None:
        return {"error": "ATS Agent not initialized."}
    try:
        print("--- Node: Embeddings/Similarity ---")
        analysis = deep_ats.embedding_agent.compare_resume_with_jd(
            state["data"]["resume"]["chunks"],
            state["data"]["job_description"]["chunks"]
        )
        deep_ats.embedding_agent.store_resume_embedding(
            state["application_id"],
            state["data"]["resume"]["chunks"],
            {
                "candidate_name": state["data"]["candidate_name"],
                "job_id": state["data"]["job_id"],
                "job_title": state["data"]["job_title"]
            }
        )
        return {"semantic_analysis": analysis}
    except Exception as e:
        return {"error": str(e)}

def node_skills(state: ATSProcessingState) -> ATSProcessingState:
    if state.get("error"):
        return state
    if deep_ats is None:
        return {"error": "ATS Agent not initialized."}
    try:
        print("--- Node: Skill Extraction ---")
        analysis = deep_ats.skill_agent.analyze_application(
            state["data"]["resume"]["full_text"],
            state["data"]["job_description"]["full_text"]
        )
        analysis["candidate_name"] = state["data"]["candidate_name"]
        deep_ats.skill_agent.store_skills(state["application_id"], analysis)
        return {"skill_analysis": analysis}
    except Exception as e:
        return {"error": str(e)}

def node_llm_eval(state: ATSProcessingState) -> ATSProcessingState:
    if state.get("error"):
        return state
    if deep_ats is None:
        return {"error": "ATS Agent not initialized."}
    try:
        print("--- Node: LLM Evaluation ---")
        context = deep_ats.llm_agent.build_context(
            state["data"],
            state["semantic_analysis"],
            state["skill_analysis"]
        )
        evaluation = deep_ats.llm_agent.generate_ats_evaluation(
            context,
            state["data"]["resume"]["full_text"],
            state["data"]["job_description"]["full_text"]
        )
        return {"llm_evaluation": evaluation}
    except Exception as e:
        return {"error": str(e)}

def node_scoring_and_persist(state: ATSProcessingState) -> ATSProcessingState:
    if state.get("error"):
        return state
    if deep_ats is None:
        return {"error": "ATS Agent not initialized."}
    try:
        print("--- Node: Scoring & Persistence ---")
        scores = {
            "semantic_similarity": state["semantic_analysis"].get("overall_score", 0),
            "skill_match": state["skill_analysis"]["comparison"]["skills"].get("match_percentage", 0),
            "experience_match": 100 if state["skill_analysis"]["comparison"].get("experience_match") else 50,
            "education_match": 75 if state["skill_analysis"]["resume"].get("education", {}).get("degrees") else 50,
            "llm_score": state["llm_evaluation"].get("ats_score", 50) if state.get("llm_evaluation") else 50
        }

        final_score = deep_ats.calculate_final_score(scores)

        results = {
            "application_id": str(state["application_id"]),
            "candidate_name": state["data"].get("candidate_name"),
            "job_title": state["data"].get("job_title"),
            "company": state["data"].get("company"),
            "final_ats_score": final_score,
            "component_scores": scores,
            "semantic_analysis": state["semantic_analysis"],
            "skill_analysis": {
                "match_percentage": state["skill_analysis"]["comparison"]["skills"].get("match_percentage"),
                "matching_skills": state["skill_analysis"]["comparison"]["skills"].get("matching_skills"),
                "missing_skills": state["skill_analysis"]["comparison"]["skills"].get("missing_skills")
            },
            "llm_evaluation": state.get("llm_evaluation"),
            "timestamp": datetime.utcnow()
        }

        deep_ats._update_mongodb(state["application_id"], results)
        deep_ats._display_results(results)
        return {"final_result": results}
    except Exception as e:
        return {"error": str(e)}

def build_ats_graph():
    """
    Build and compile the LangGraph workflow for ATS processing.
    Compatible with langgraph 0.0.x (NO START/END support).
    """
    builder = StateGraph(ATSProcessingState)

    # --- Add workflow nodes ---
    builder.add_node("retrieval", node_retrieval)
    builder.add_node("embedding", node_embedding)
    builder.add_node("skills", node_skills)
    builder.add_node("llm", node_llm_eval)
    builder.add_node("scoring", node_scoring_and_persist)

    # --- Define workflow edges ---
    builder.set_entry_point("retrieval")       # START here
    builder.add_edge("retrieval", "embedding")
    builder.add_edge("embedding", "skills")
    builder.add_edge("skills", "llm")
    builder.add_edge("llm", "scoring")        # scoring is last node

    # --- Define finish point correctly (NO END constant needed) ---
    builder.set_finish_point("scoring")

    # Compile and return the graph
    return builder.compile()


# Initialize ATS graph safely
ats_app = None
try:
    if deep_ats:
        ats_app = build_ats_graph()
        print("DeepAgent ATS Graph compiled successfully.")
    else:
        print("DeepAgent not available — ATS graph not compiled.")
except Exception as e:
    print(f"⚠️ Error compiling ATS graph: {e}")
    ats_app = None

# -------------------------
# FastAPI models & endpoints (kept from your code)
# -------------------------
class JDWorkflowState(TypedDict):
    company_name: str
    job_role: str
    job_description: str
    approved: bool
    posting_result: str
    demo_post_result: str
    applications: List[Dict[str, Any]]
    application_count: int
    days_monitoring: int
    enough_applications: bool
    shortlist: List[Dict[str, Any]]
    interviews: List[Dict[str, Any]]
    location: Optional[str]
    interview_results: List[Dict[str, Any]]
    offer_candidates: List[Dict[str, Any]]
    offers: List[Dict[str, Any]]
    onboarded_employees: List[Dict[str, Any]]

class WorkflowRequest(BaseModel):
    company_name: Optional[str] = None
    job_role: Optional[str] = None
    companyName: Optional[str] = None
    jobTitle: Optional[str] = None
    jobRole: Optional[str] = None
    location: Optional[str] = None

    def get_company_name(self) -> str:
        return self.company_name or self.companyName or ""

    def get_job_role(self) -> str:
        return self.job_role or self.jobRole or self.jobTitle or ""

    def get_location(self) -> Optional[str]:
        return self.location

class JDGenerateRequest(BaseModel):
    company_name: Optional[str] = None
    job_role: Optional[str] = None
    companyName: Optional[str] = None
    jobTitle: Optional[str] = None
    location: Optional[str] = None

    def get_company_name(self) -> str:
        return self.company_name or self.companyName or ""

    def get_job_role(self) -> str:
        return self.job_role or self.jobTitle or ""

    def get_location(self) -> Optional[str]:
        return self.location

class JDGenerateResponse(BaseModel):
    job_description: str
    company_name: str
    job_role: str

class ApplicationsResponse(BaseModel):
    applications: List[Dict[str, Any]]
    count: int

class ShortlistRequest(BaseModel):
    job_description: str

class ShortlistResponse(BaseModel):
    shortlist: List[Dict[str, Any]]
    count: int

class InterviewRequest(BaseModel):
    shortlist: List[Dict[str, Any]]
    job_role: str

class InterviewResponse(BaseModel):
    interviews: List[Dict[str, Any]]
    count: int

# Helper functions (unchanged logic)
def fetch_applications_from_mongodb() -> List[Dict[str, Any]]:
    client = get_mongodb_connection()
    if not client:
        return []
    try:
        db = client[Config.DATABASE_NAME]
        job_applications = db.get_collection(Config.APPLICATIONS_COLLECTION)
        applications = job_applications.find({"resumeData": {"$exists": True, "$ne": None}})
        app_list = []
        for app in applications:
            app_list.append({
                "_id": str(app["_id"]),
                "application_id": str(app["_id"]),
                "candidate_name": app.get("candidateName", "Unknown"),
                "email": app.get("candidateEmail", ""),
                "phone": app.get("candidatePhone", ""),
                "job_ref": str(app.get("job", "")),
                "status": app.get("status", "SUBMITTED"),
                "resume_filename": app.get("resumeFileName", ""),
                "applied_at": str(app.get("appliedAt", datetime.utcnow()))
            })
        return app_list
    except Exception as e:
        print(f"Error fetching applications: {e}")
        return []
    finally:
        try:
            client.close()
        except Exception:
            pass

def save_job_to_mongodb(job_data: Dict[str, Any]) -> bool:
    client = get_mongodb_connection()
    if not client:
        return False
    try:
        db = client[Config.DATABASE_NAME]
        jobs_collection = db[Config.JOBS_COLLECTION]
        job_document = {
            "company_name": job_data.get("company"),
            "job_role": job_data.get("role"),
            "description": job_data.get("description"),
            "status": job_data.get("status", "published"),
            "created_at": job_data.get("timestamp"),
            "posted_at": str(datetime.utcnow())
        }
        jobs_collection.insert_one(job_document)
        return True
    except Exception as e:
        print(f"Error saving job to MongoDB: {e}")
        return False
    finally:
        try:
            client.close()
        except Exception:
            pass

# JD agent node
def jd_generator_node(state: JDWorkflowState) -> Dict[str, Any]:
    print(f"🚀 Invoking Advanced JD Agent for {state.get('job_role')} at {state.get('company_name')}...")
    job_location = state.get("location", "Remote")
    try:
        final_jd = jd_agent.generate(
            company_name=state.get("company_name", ""),
            job_role=state.get("job_role", ""),
            location=job_location
        )
        return {"job_description": final_jd.get("job_description", "")}
    except Exception as e:
        print(f"❌ JD Agent Error: {e}")
        return {"job_description": f"Job Description for {state.get('job_role')} (Generation Failed: {e})"}

def jd_approval_node(state: JDWorkflowState) -> Dict[str, Any]:
    jr = state.get("job_role", "") or ""
    approved = any(k in jr.lower() for k in ["ai", "ml", "data", "engineer", "developer"])
    return {"approved": approved}

def jd_post_node(state: JDWorkflowState) -> Dict[str, Any]:
    if not state.get("approved"):
        return {"posting_result": "JD rejected, not posted."}
    payload = {
        "company": state.get("company_name"),
        "role": state.get("job_role"),
        "description": state.get("job_description"),
        "timestamp": str(datetime.utcnow()),
        "status": "published"
    }
    saved = save_job_to_mongodb(payload)
    try:
        headers = {"Content-Type": "application/json", "User-Agent": "SmartHire-AI-Service/1.0"}
        res = requests.post(WEBHOOK_URL, json=payload, headers=headers, timeout=10)
        msg = "Posted successfully" if res.status_code in (200, 201) else f"Webhook error: {res.status_code}"
        return {"posting_result": f"{msg}. Saved to DB: {saved}"}
    except Exception as e:
        return {"posting_result": f"Webhook failed: {e}. Saved to DB: {saved}"}

def application_monitoring_node(state: JDWorkflowState) -> Dict[str, Any]:
    applications = fetch_applications_from_mongodb()
    return {
        "applications": applications,
        "application_count": len(applications),
        "days_monitoring": 1,
        "enough_applications": len(applications) >= 5
    }

def shortlisting_node(state: JDWorkflowState) -> Dict[str, Any]:
    resumes = state.get("applications", [])
    if not resumes or not ats_app:
        return {"shortlist": []}
    scored_resumes = []
    print(f"\n🚀 Starting LangGraph ATS Analysis for {len(resumes)} applications...")
    for i, app in enumerate(resumes, 1):
        app_id_str = app.get("_id") or app.get("application_id")
        if not app_id_str:
            continue
        try:
            app_id = ObjectId(app_id_str)
            print(f"Processing {i}/{len(resumes)}: {app.get('candidate_name')}")
            result_state = ats_app.invoke({"application_id": app_id})
            if result_state.get("error"):
                print(f"❌ Graph Error for {app_id_str}: {result_state['error']}")
                continue
            final_data = result_state.get("final_result")
            if final_data:
                app["final_score"] = final_data["final_ats_score"]
                app["ats_score"] = final_data["final_ats_score"]
                llm_eval = final_data.get("llm_evaluation", {})
                app["explanations"] = [
                    f"Skill Match: {final_data['skill_analysis']['match_percentage']:.1f}%",
                    f"Decision: {llm_eval.get('decision', 'N/A')}",
                    f"Interview: {llm_eval.get('interview_recommendation', 'N/A')}"
                ]
                scored_resumes.append(app)
        except Exception as e:
            print(f"❌ Execution error for {app_id_str}: {e}")
            continue
    shortlist = sorted(scored_resumes, key=lambda x: float(x.get("final_score", 0.0)), reverse=True)[:3]
    return {"shortlist": shortlist}

def interview_scheduling_node(state: JDWorkflowState) -> Dict[str, Any]:
    shortlist = state.get("shortlist", [])
    job_role = state.get("job_role", "Candidate")
    if not shortlist:
        return {"interviews": []}
    if scheduler is None:
        print("⚠️ Scheduler not initialized; cannot schedule interviews.")
        return {"interviews": []}
    try:
        interviews = scheduler.schedule_interviews(shortlist=shortlist, job_role=job_role)
        return {"interviews": interviews}
    except Exception as e:
        print(f"❌ Scheduling Error: {e}")
        return {"interviews": []}

def node_conduct_interview(state: JDWorkflowState) -> Dict[str, Any]:
    interviews = state.get("interviews", [])
    interview_results = []
    print(f"🎙️ Checking Voice Interview results for {len(interviews)} scheduled candidates...")
    for interview in interviews:
        candidate_name = interview.get("candidate_name", "Unknown Candidate")
        interview_id = interview.get("interview_id")
        if interview_id:
            ats_score = interview.get("ats_score", 0)
            passed_interview = ats_score >= 80
            interview_results.append({
                "candidate_name": candidate_name,
                "interview_id": interview_id,
                "application_id": interview.get("application_id"),
                "status": "COMPLETED" if passed_interview else "FAILED",
                "recommendation": "PROCEED_TO_OFFER" if passed_interview else "REJECTED",
                "final_interview_score": 95 if passed_interview else 40
            })
    passed_candidates = [
        res for res in interview_results
        if res["status"] == "COMPLETED" and res["recommendation"] == "PROCEED_TO_OFFER"
    ]
    return {"interview_results": interview_results, "offer_candidates": passed_candidates}

def node_offer_letter(state: JDWorkflowState) -> Dict[str, Any]:
    if offer_onboarding_agent is None:
        return {"offers": []}
    return offer_onboarding_agent.node_offer_letter(state)

def node_onboarding(state: JDWorkflowState) -> Dict[str, Any]:
    if offer_onboarding_agent is None:
        return {"onboarded_employees": []}
    return offer_onboarding_agent.node_onboarding(state)

# FastAPI app
fastapi_app = FastAPI(title="Smart Hire AI - LangGraph Edition")
fastapi_app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

# include voice router if available
try:
    fastapi_app.include_router(voice_router)
except Exception as e:
    print(f"⚠️ Could not include voice router: {e}")

@fastapi_app.get("/")
async def root():
    return {"message": "Smart Hire AI (LangGraph + Voice) is running", "status": "ok"}

@fastapi_app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "mongo": bool(MONGODB_URI),
        "deep_ats_loaded": bool(ats_app),
        "scheduler_ready": bool(scheduler),
        "offer_agent_ready": bool(offer_onboarding_agent),
        "voice_router": True
    }

@fastapi_app.post("/generate-jd", response_model=JDGenerateResponse)
async def generate_jd(request: JDGenerateRequest):
    state = {
        "company_name": request.get_company_name(),
        "job_role": request.get_job_role(),
        "location": request.get_location()
    }
    res = jd_generator_node(state)
    return {
        "job_description": res.get("job_description"),
        "company_name": request.get_company_name(),
        "job_role": request.get_job_role()
    }

@fastapi_app.post("/post-jd")
async def post_jd(request: WorkflowRequest):
    state = {
        "company_name": request.get_company_name(),
        "job_role": request.get_job_role(),
        "job_description": "",
        "approved": True,
        "location": request.get_location()
    }
    if not state.get("job_description"):
        state.update(jd_generator_node(state))
    post_res = jd_post_node(state)
    return {"status": "success", "posting_result": post_res["posting_result"], "job_description": state.get("job_description")}

@fastapi_app.get("/applications", response_model=ApplicationsResponse)
async def get_apps():
    res = application_monitoring_node({})
    return {"applications": res["applications"], "count": res["application_count"]}

@fastapi_app.post("/shortlist", response_model=ShortlistResponse)
async def shortlist_endpoint(request: ShortlistRequest):
    mon_res = application_monitoring_node({})
    wf_state = {
        "job_description": request.job_description,
        "applications": mon_res["applications"],
        "shortlist": []
    }
    result = shortlisting_node(wf_state)
    return {"shortlist": result["shortlist"], "count": len(result["shortlist"])}

@fastapi_app.post("/schedule-interviews", response_model=InterviewResponse)
async def schedule_endpoint(request: InterviewRequest):
    state = {"shortlist": request.shortlist, "job_role": request.job_role}
    res = interview_scheduling_node(state)
    return {"interviews": res["interviews"], "count": len(res["interviews"])}

@fastapi_app.post("/conduct-interviews")
async def conduct_interviews_endpoint(request: InterviewRequest):
    state = {"interviews": request.shortlist, "job_role": request.job_role}
    interview_res = node_conduct_interview(state)
    state.update(interview_res)
    offer_res = node_offer_letter(state)
    state.update(offer_res)
    onboard_res = node_onboarding(state)
    return {
        "status": "success",
        "interview_checks": interview_res["interview_results"],
        "offers_extended": len(offer_res.get("offers", [])),
        "onboarding_initiated": len(onboard_res.get("onboarded_employees", []))
    }

@fastapi_app.post("/process-all-applications")
async def process_all():
    if not ats_app:
        raise HTTPException(status_code=503, detail="DeepAgent not initialized")
    try:
        apps = fetch_applications_from_mongodb()
        processed = 0
        for app in apps:
            app_id = ObjectId(app["_id"])
            try:
                ats_app.invoke({"application_id": app_id})
                processed += 1
            except Exception as e:
                print(f"Error in batch: {e}")
        return {"status": "success", "processed": processed, "total": len(apps)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    print("="*60)
    print("Starting Smart Hire AI - LangGraph Orchestrator")
    print(f"Model cache used: {os.environ.get('HF_HOME')}")
    print("="*60)
    uvicorn.run(fastapi_app, host="0.0.0.0", port=int(os.getenv("PORT", 5001)))

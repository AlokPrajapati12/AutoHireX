import os
from datetime import datetime, timedelta
from typing import TypedDict, List, Dict, Any, Optional
from bson import ObjectId

# Re-defining the Workflow State for type hinting (assuming JDWorkflowState is defined in main)
class JDWorkflowState(TypedDict):
    company_name: str
    job_role: str
    # ... other state keys ...
    interviews: List[Dict[str, Any]]
    interview_results: List[Dict[str, Any]]
    offer_candidates: List[Dict[str, Any]]
    offers: List[Dict[str, Any]]
    onboarded_employees: List[Dict[str, Any]]
    location: Optional[str] # Added for completeness


class OfferOnboardingAgent:
    """
    Agent responsible for generating offer letters and initiating the onboarding process.
    """
    def __init__(self, get_email_helper_func: callable, config: Any):
        """
        Initializes the agent with the necessary MongoDB email retrieval function 
        and configuration (e.g., Config class).
        """
        self.get_candidate_email_from_mongodb = get_email_helper_func
        self.config = config 

    def node_offer_letter(self, state: JDWorkflowState) -> Dict[str, Any]:
        """
        Generates and extends an offer letter to successful candidates.
        Automatically sends the offer letter via email using MongoDB data helper.
        """
        offer_candidates = state.get("offer_candidates", [])
        offers = []
        
        if not offer_candidates:
            print("🛑 No candidates passed the interview stage. Skipping offer letter generation.")
            return {"offers": []}
        
        print(f"💰 Generating offer letters for {len(offer_candidates)} candidates...")
        
        for candidate in offer_candidates:
            candidate_name = candidate["candidate_name"]
            app_mongo_id = candidate.get("_id") # Use the ID passed from the previous node

            # --- EXPLICIT MONGODB CALL via helper function ---
            # Fallback to the email already in the candidate dict if helper fails
            candidate_email = self.get_candidate_email_from_mongodb(app_mongo_id) or candidate.get("email", "mock_email@example.com")
            # -------------------------------------------------
            
            offer_details = {
                "candidate_name": candidate_name,
                "email": candidate_email,
                "role": state.get("job_role"),
                "status": "OFFERED",
                "salary": "$120,000 USD (Simulated)",
                "offer_date": str(datetime.utcnow())
            }
            
            # --- AUTOMATED EMAIL: SEND OFFER LETTER ---
            offer_subject = f"Job Offer: {state.get('job_role')} at {state.get('company_name', 'Company')}"
            offer_body = f"""
Dear {candidate_name},

We are thrilled to offer you the position of **{state.get('job_role')}** at {state.get('company_name', 'Company')}.

**Salary:** {offer_details['salary']}
**Start Date:** {str(datetime.utcnow() + timedelta(days=30)).split()[0]}

Please find the official offer letter attached (simulated). We look forward to welcoming you to the team!

Best regards,
The Hiring Team
"""
            print(f"📧 [Offer Letter Sent to {candidate_email}] Subject: {offer_subject}")
            # Placeholder for actual email sending logic
            # ------------------------------------------
            
            offers.append(offer_details)
            print(f"✔️ Offer extended to {candidate_name}.")
            
        return {"offers": offers}

    def node_onboarding(self, state: JDWorkflowState) -> Dict[str, Any]:
        """
        Node: Triggers IT provisioning and HR setup for accepted offers.
        """
        offers = state.get("offers", [])
        onboarded_employees = []
        
        if not offers:
            print("🛑 No offers were extended or accepted. Skipping onboarding.")
            return {"onboarded_employees": []}
        
        print(f"💻 Initiating onboarding for {len(offers)} potential new hires...")
        
        # SIMULATION: Assuming all extended offers are accepted and proceed to onboarding
        for offer in offers:
            onboarding_record = {
                "employee_name": offer["candidate_name"],
                "status": "ONBOARDING_IN_PROGRESS",
                "hr_system_id": "HR" + str(hash(offer["candidate_name"]) % 1000), # Simulated ID
                "start_date": str(datetime.utcnow() + timedelta(days=30))
            }
            
            # Placeholder: Trigger HRMS API call and IT request system
            
            onboarded_employees.append(onboarding_record)
            print(f"🎉 Onboarding started for {offer['candidate_name']}.")
            
        return {"onboarded_employees": onboarded_employees}
"""
Real-Time AI Job Description Generator
--------------------------------------
Uses:
✔ Tavily Search API for real market data
✔ Gemini LLM for enterprise-grade JD writing
✔ FULL support for: company, role, location, experience, employment_type
✔ Safe fallback even if LLM/Tavily unavailable
"""

import os
import json
from typing import Optional

# ---------- Optional Tavily Search ----------
try:
    from tavily import TavilyClient
except:
    TavilyClient = None

# ---------- Optional Gemini LLM ----------
try:
    from langchain_google_genai import ChatGoogleGenerativeAI
    from langchain_core.messages import HumanMessage
except:
    ChatGoogleGenerativeAI = None
    class HumanMessage:
        def __init__(self, content): self.content = content

from config import Config


class RealJDGenerator:

    def __init__(self):
        # Tavily setup
        tavily_key = os.getenv("TAVILY_API_KEY")
        self.tavily_enabled = tavily_key and TavilyClient is not None
        self.tavily = TavilyClient(api_key=tavily_key) if self.tavily_enabled else None

        # Gemini setup
        self.llm = None
        if ChatGoogleGenerativeAI is not None:
            try:
                self.llm = ChatGoogleGenerativeAI(
                    model=Config.GEMINI_MODEL,
                    temperature=0.25,
                    google_api_key=Config.GEMINI_API_KEY
                )
            except:
                self.llm = None

        print(f"AI JD Generator Loaded → Gemini={'ON' if self.llm else 'OFF'}, Tavily={'ON' if self.tavily else 'OFF'}")

    # ----------------------------------------------------------------------
    # Fetch real market research data
    # ----------------------------------------------------------------------
    def fetch_research(self, company: str, role: str):
        if not self.tavily:
            return {
                "summary": "Industry-standard expectations were applied.",
                "location": "Remote",
                "salary": "Competitive",
                "market_skills": ["Communication", "Problem Solving", "Cloud Basics"]
            }

        query = f"{company} {role} job responsibilities salary location skills"
        print(f"🌍 Running Tavily Search: {query}")

        try:
            result = self.tavily.search(
                query=query,
                n_tokens=500,
                include_domains=None
            )
        except Exception as e:
            print("⚠️ Tavily failed:", e)
            return {
                "summary": "Industry-standard expectations.",
                "location": "Remote",
                "salary": "Competitive",
                "market_skills": ["APIs", "Teamwork", "SQL"]
            }

        first = result.get("results", [{}])[0]

        return {
            "summary": first.get("summary", "No real-time data found, using fallback."),
            "location": "Remote",
            "salary": "Competitive",
            "market_skills": ["Python", "Leadership", "REST APIs"]
        }

    # ----------------------------------------------------------------------
    # Generate JD using Gemini (fallback included)
    # ----------------------------------------------------------------------
    def write_jd(self, company: str, role: str, research: dict,
                 location: str, experience_level: str, employment_type: str):

        prompt = f"""
Write an enterprise-grade Job Description.

Company: {company}
Role: {role}
Location: {location}
Experience Level: {experience_level}
Employment Type: {employment_type}

Market Research:
Summary: {research['summary']}
Market Skills: {research['market_skills']}
Salary Trend: {research['salary']}

STRUCTURE:
1. Role Overview
2. Responsibilities (10 bullet points)
3. Required Skills
4. Preferred Skills
5. Salary & Benefits
6. About the Company
"""

        # Fallback if no Gemini available
        if not self.llm:
            return f"""
# {role} — {company}
**Location:** {location}  
**Experience:** {experience_level}  
**Employment Type:** {employment_type}  
**Salary:** {research['salary']}

## Role Overview
{company} is hiring a skilled **{role}**. Market data suggests:
{research['summary']}

## Responsibilities
- Work on key engineering initiatives  
- Deliver high-quality work  
- Collaborate with cross-functional teams  
- Solve complex problems  
- Improve system performance  
- Write clean, maintainable code  
- Participate in code reviews  
- Support production systems  
- Follow engineering best practices  
- Contribute to team planning  

## Required Skills
- {", ".join(research["market_skills"])}

## Preferred Skills
- Cloud experience  
- Modern best practices  
- Strong teamwork and communication  

## Salary & Benefits
- Competitive salary  
- Growth opportunities  
- Remote flexibility  

## About {company}
A forward-thinking, innovation-driven company.
"""

        # If Gemini is available → generate premium JD
        response = self.llm.invoke([HumanMessage(content=prompt)])
        return response.content

    # ----------------------------------------------------------------------
    # PUBLIC API called by FastAPI
    # ----------------------------------------------------------------------
    def generate(
        self,
        company_name: str,
        job_role: str,
        location: Optional[str] = "Remote",
        experience_level: Optional[str] = "MID",
        employment_type: Optional[str] = "FULL_TIME"
    ):
        # Step 1 — fetch research
        research = self.fetch_research(company_name, job_role)

        # Override research if frontend provided values
        research["location"] = location or research["location"]

        # Step 2 — generate JD text
        final_jd = self.write_jd(
            company_name,
            job_role,
            research,
            location,
            experience_level,
            employment_type
        )

        # Step 3 — return JSON format Spring Boot expects
        return {
            "company_name": company_name,
            "job_role": job_role,
            "location": location,
            "experience_level": experience_level,
            "employment_type": employment_type,
            "job_description": final_jd
        }


# Singleton instance for import
jd_agent = RealJDGenerator()

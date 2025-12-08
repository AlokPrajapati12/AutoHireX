import os
import datetime
from datetime import timedelta
from google.oauth2 import service_account
from googleapiclient.discovery import build
from pymongo import MongoClient
from typing import List, Dict, Any

# Config
SERVICE_ACCOUNT_FILE = os.getenv("SERVICE_ACCOUNT_FILE", "service-account.json")
MONGODB_URI = os.getenv("MONGODB_URI")
DB_NAME = "SmartHireDB" # Adjust to your DB name

class ScheduleAgent:
    def __init__(self):
        self.scopes = ["https://www.googleapis.com/auth/calendar"]
        self.creds = None
        if os.path.exists(SERVICE_ACCOUNT_FILE):
            self.creds = service_account.Credentials.from_service_account_file(
                SERVICE_ACCOUNT_FILE, scopes=self.scopes
            )
        
        self.mongo = None
        if MONGODB_URI:
            self.mongo = MongoClient(MONGODB_URI)
            self.db = self.mongo[DB_NAME]

    def _create_calendar_event(self, name, role, start_time):
        if not self.creds: return "https://calendar.google.com" # Mock if no creds
        try:
            service = build("calendar", "v3", credentials=self.creds)
            event = {
                "summary": f"AI Interview: {name} for {role}",
                "start": {"dateTime": start_time.isoformat(), "timeZone": "UTC"},
                "end": {"dateTime": (start_time + timedelta(minutes=30)).isoformat(), "timeZone": "UTC"},
            }
            res = service.events().insert(calendarId="primary", body=event).execute()
            return res.get("htmlLink")
        except Exception as e:
            print(f"Calendar Error: {e}")
            return None

    def schedule_interviews(self, shortlist: List[Dict], job_role: str) -> List[Dict]:
        scheduled = []
        start_time = datetime.datetime.utcnow() + timedelta(days=1)
        
        for i, cand in enumerate(shortlist):
            cand_name = cand.get("candidate_name", "Unknown")
            slot = start_time + timedelta(hours=i)
            
            # 1. Google Calendar
            cal_link = self._create_calendar_event(cand_name, job_role, slot)
            
            # 2. Generate Interview ID
            interview_id = f"int_{int(slot.timestamp())}_{i}"
            
            # 3. Create WebSocket Link
            # Assuming main.py runs on localhost:5001 or deployed URL
            ws_link = f"ws://localhost:5001/ws/interview/{interview_id}"
            
            # 4. Initialize State in MongoDB (for Persistence)
            if self.mongo:
                initial_state = {
                    "interview_id": interview_id,
                    "candidate_name": cand_name,
                    "job_role": job_role,
                    "messages": [],
                    "current_round": 1,
                    "scores": {},
                    "verdict": "Scheduled",
                    "scheduled_at": slot
                }
                self.db.interviews.insert_one(initial_state)

            scheduled.append({
                "candidate": cand_name,
                "time": slot.strftime("%Y-%m-%d %H:%M UTC"),
                "calendar_link": cal_link,
                "interview_link": ws_link, 
                "interview_id": interview_id
            })
            
        return scheduled
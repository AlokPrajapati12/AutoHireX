from typing import TypedDict, List, Annotated
from langgraph.graph.message import add_messages

class InterviewState(TypedDict):
    interview_id: str
    candidate_name: str
    job_role: str
    messages: Annotated[List[dict], add_messages]  # Chat history
    current_round: int  # 1: Resume, 2: Technical, 3: Behavioral
    scores: dict  # {"round_1_score": 8.5, ...}
    verdict: str  # "Hire", "Reject", "Pending"
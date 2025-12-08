import os
from langgraph.graph import StateGraph, END
# REMOVE: from langchain_community.chat_models import ChatOllama
from langchain_core.messages import SystemMessage
from state_schema import InterviewState
from langchain_google_genai import ChatGoogleGenerativeAI
# --- Import Config ---
# Necessary to get the GEMINI_MODEL name and API key.
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config
# ---------------------

# Initialize LLM with Gemini, using the configured model name
llm = ChatGoogleGenerativeAI(
    model=Config.GEMINI_MODEL,
    temperature=0.7,
    api_key=os.getenv("GEMINI_API_KEY"),   # <-- REQUIRED FIX
)

# --- Node 1: Evaluator ---
def evaluator_node(state: InterviewState):
# ... (rest of evaluator_node function remains the same)
    """Scores the last user response."""
    messages = state["messages"]
    if len(messages) < 2 or messages[-1].type != "human":
        return {"scores": state.get("scores", {})}

    last_response = messages[-1].content
    current_round = state["current_round"]
    
    prompt = f"""
    Role: Interview Scorer.
    Context: Round {current_round}/3.
    Candidate Answer: "{last_response}"
    
    Task: Rate accuracy and relevance (0.0 to 10.0). Return ONLY the number.
    """
    try:
        score_str = llm.invoke(prompt).content
        import re
        match = re.search(r"[\d\.]+", score_str)
        score = float(match.group()) if match else 5.0
    except:
        score = 5.0

    new_scores = state.get("scores", {}).copy()
    new_scores[f"round_{current_round}_last"] = score
    return {"scores": new_scores}

# --- Node 2: Interviewer ---
def interviewer_node(state: InterviewState):
# ... (rest of interviewer_node function remains the same)
    """Generates the next question."""
    rounds = {1: "Resume Verification", 2: "Technical Skills", 3: "Behavioral Fit"}
    topic = rounds.get(state["current_round"], "General")
    
    system_msg = f"""
    You are Sarah, an AI Recruiter for {state.get('job_role', 'this role')}.
    Current Phase: Round {state['current_round']} - {topic}.
    Candidate: {state['candidate_name']}.
    
    Instructions:
    1. Acknowledge the previous answer briefly.
    2. Ask ONE short, clear question relevant to the topic.
    3. Keep it conversational (spoken style).
    """
    
    messages = [SystemMessage(content=system_msg)] + state["messages"]
    response = llm.invoke(messages)
    return {"messages": [response]}

# --- Router Logic ---
def round_router(state: InterviewState):
# ... (rest of round_router function remains the same)
    messages = state["messages"]
    human_msgs = [m for m in messages if m.type == 'human']
    
    # Advance round after 3 human interactions per round
    # (Simplified logic for demo)
    if len(human_msgs) > 0 and len(human_msgs) % 3 == 0:
        if state["current_round"] < 3:
            return "advance_round"
        else:
            return "end_interview"
    return "continue"

def advance_round_node(state: InterviewState):
    return {"current_round": state["current_round"] + 1}

def end_interview_node(state: InterviewState):
    return {"verdict": "Completed"}

# --- Build Graph ---
workflow = StateGraph(InterviewState)
workflow.add_node("evaluator", evaluator_node)
workflow.add_node("interviewer", interviewer_node)
workflow.add_node("advancer", advance_round_node)
workflow.add_node("ender", end_interview_node)

workflow.set_entry_point("evaluator")
workflow.add_edge("evaluator", "interviewer")

workflow.add_conditional_edges(
    "interviewer",
    round_router,
    {
        "continue": END,
        "advance_round": "advancer",
        "end_interview": "ender"
    }
)

workflow.add_edge("advancer", "interviewer")
workflow.add_edge("ender", END)

interview_graph_app = workflow.compile()
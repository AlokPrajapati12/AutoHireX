import io
import os
import time
import numpy as np
import scipy.io.wavfile as wavfile
import soundfile as sf
import whisper
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from pymongo import MongoClient
from langchain_core.messages import HumanMessage

# Import Graph
from agents.interview_graph import interview_graph_app

# gTTS Import
try:
    from gtts import gTTS
except ImportError:
    gTTS = None

voice_router = APIRouter()

# --- Config ---
MONGODB_URI = os.getenv("MONGODB_URI")
DB_NAME = "SmartHireDB"

# --- Load TTS ---
print("⏳ Loading Voice Models...")
tts_model = gTTS
if not tts_model:
    print("⚠️ gTTS not available — install using: pip install gTTS")

# --- Load Whisper ---
# --- Load Whisper ---
try:
    WHISPER_CACHE = r"D:\Java Project\AngularSpringWebsiteUp\ai-service\model_cache\whisper"
    os.makedirs(WHISPER_CACHE, exist_ok=True)

    whisper_model = whisper.load_model("base.en", download_root=WHISPER_CACHE)
    print("✓ Whisper model loaded successfully")
except Exception as e:
    whisper_model = None
    print("⚠️ Whisper Model not loaded:", e)



# --- Database helper ---
def get_db():
    if MONGODB_URI:
        client = MongoClient(MONGODB_URI)
        return client[DB_NAME]
    return None


# --- Speech-to-text ---
async def transcribe(audio_bytes):
    if not whisper_model:
        print("❌ Whisper model not initialized")
        return ""

    try:
        audio_data, samplerate = sf.read(io.BytesIO(audio_bytes))

        if len(audio_data.shape) > 1:
            audio_data = np.mean(audio_data, axis=1)

        result = whisper_model.transcribe(audio_data)
        return result.get("text", "").strip()

    except Exception as e:
        print("STT Error:", e)
        return ""


# --- Text-to-speech ---
async def speak(text):
    if not tts_model:
        print("⚠️ TTS unavailable")
        return b""

    try:
        tts = gTTS(text=text, lang="en", slow=False)
        mp3_io = io.BytesIO()
        tts.write_to_fp(mp3_io)
        mp3_io.seek(0)
        return mp3_io.read()
    except Exception as e:
        print("TTS Error:", e)
        return b""


# --- WebSocket API ---
@voice_router.websocket("/ws/interview/{interview_id}")
async def websocket_endpoint(websocket: WebSocket, interview_id: str):
    await websocket.accept()
    db = get_db()

    state_doc = db.interviews.find_one({"interview_id": interview_id}) if db else None

    if not state_doc:
        state_doc = {
            "candidate_name": "Candidate",
            "job_role": "General Role",
            "messages": [],
            "current_round": 1,
            "scores": {}
        }

    if "_id" in state_doc:
        del state_doc["_id"]

    greeting = f"Hi {state_doc.get('candidate_name')}. I am Sarah. We are interviewing for the {state_doc.get('job_role')} position. Shall we begin?"

    await websocket.send_bytes(await speak(greeting))

    try:
        while True:
            data = await websocket.receive_bytes()
            user_text = await transcribe(data)

            if not user_text:
                continue

            print(f"User ({interview_id}): {user_text}")

            state_doc["messages"].append(HumanMessage(content=user_text))

            result = interview_graph_app.invoke(state_doc)

            ai_msg = result["messages"][-1].content
            print(f"AI: {ai_msg}")

            await websocket.send_bytes(await speak(ai_msg))

            if db:
                db.interviews.update_one(
                    {"interview_id": interview_id},
                    {"$set": {
                        "messages": [m.dict() for m in result["messages"]],
                        "current_round": result.get("current_round"),
                        "scores": result.get("scores", {}),
                        "verdict": result.get("verdict", "Pending")
                    }},
                    upsert=True
                )

            state_doc = result

            if result.get("verdict") == "Completed":
                await websocket.close()
                break

    except WebSocketDisconnect:
        print(f"Disconnected: {interview_id}")
    except Exception as e:
        print("WS Error:", e)

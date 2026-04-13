import json
import joblib
import numpy as np
import pandas as pd
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sklearn.preprocessing import LabelEncoder

from app.train import ensure_model, CAT_COLS

import os as _os
MODEL_DIR = _os.environ.get("MODEL_DIR", "/app/models")
_models: dict = {}

CLUSTER_LABELS = ["At-Risk", "Average", "High-Performing"]
CLUSTER_DESC = {
    0: "Student shows signs of academic difficulty. Targeted support is recommended.",
    1: "Student performs at an average level. Moderate risk of failure.",
    2: "Student demonstrates strong academic performance. Low risk.",
}


@asynccontextmanager
async def lifespan(app: FastAPI):
    ensure_model()
    _models["scaler"]       = joblib.load(f"{MODEL_DIR}/scaler.pkl")
    _models["pca"]          = joblib.load(f"{MODEL_DIR}/pca.pkl")
    _models["kmeans"]       = joblib.load(f"{MODEL_DIR}/kmeans.pkl")
    _models["rf"]           = joblib.load(f"{MODEL_DIR}/rf_classifier.pkl")
    _models["feature_cols"] = joblib.load(f"{MODEL_DIR}/feature_cols.pkl")
    _models["encoders"]     = joblib.load(f"{MODEL_DIR}/encoders.pkl")
    with open(f"{MODEL_DIR}/metrics.json") as f:
        _models["metrics"] = json.load(f)
    print("[ML] All models loaded and ready.")
    yield


app = FastAPI(title="ML Service — Student Performance Predictor", version="1.0.0", lifespan=lifespan)



class StudentInput(BaseModel):
    school:     str = "GP"
    sex:        str = "M"
    age:        int = 17
    address:    str = "U"
    famsize:    str = "GT3"
    Pstatus:    str = "T"
    Medu:       int = 2
    Fedu:       int = 2
    Mjob:       str = "other"
    Fjob:       str = "other"
    reason:     str = "course"
    guardian:   str = "mother"
    traveltime: int = 2
    studytime:  int = 2
    failures:   int = 0
    schoolsup:  str = "no"
    famsup:     str = "yes"
    paid:       str = "no"
    activities: str = "no"
    nursery:    str = "yes"
    higher:     str = "yes"
    internet:   str = "yes"
    romantic:   str = "no"
    famrel:     int = 4
    freetime:   int = 3
    goout:      int = 3
    Dalc:       int = 1
    Walc:       int = 1
    health:     int = 3
    absences:   int = 6
    G1:         int = 10
    G2:         int = 10


def _to_pca_vector(student: StudentInput) -> np.ndarray:
    row = student.model_dump()
    df = pd.DataFrame([row])

    for col in CAT_COLS:
        le: LabelEncoder = _models["encoders"].get(col)
        if le is not None:
            val = df[col].astype(str)
            # Handle unseen labels gracefully
            known = set(le.classes_)
            df[col] = val.apply(lambda v: le.transform([v])[0] if v in known else 0)

    feature_cols = _models["feature_cols"]
    X = df[feature_cols].values.astype(float)
    X_scaled = _models["scaler"].transform(X)
    X_pca    = _models["pca"].transform(X_scaled)
    return X_pca


# ── Endpoints ──────────────────────────────────────────────────────────────────

@app.get("/health", tags=["System"])
def health():
    return {"status": "ok", "service": "ml-service"}


@app.get("/model-info", tags=["System"])
def model_info():
    """Return training metrics and model configuration."""
    return _models.get("metrics", {})


@app.post("/predict", tags=["ML"])
def predict(student: StudentInput):
    """
    Predict whether a student will pass (G3 >= 10) or fail their exam/retake.

    Returns:
    - prediction: "pass" or "fail"
    - probability_pass: confidence score
    """
    try:
        X = _to_pca_vector(student)
        pred  = int(_models["rf"].predict(X)[0])
        proba = _models["rf"].predict_proba(X)[0]
        return {
            "prediction":       "pass" if pred == 1 else "fail",
            "probability_pass": round(float(proba[1]), 4),
            "probability_fail": round(float(proba[0]), 4),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/cluster", tags=["ML"])
def cluster(student: StudentInput):
    """
    Assign a student to a performance cluster (K-Means, k=3).

    Returns:
    - cluster: 0 (At-Risk), 1 (Average), 2 (High-Performing)
    - label: human-readable cluster name
    - description: actionable insight
    """
    try:
        X = _to_pca_vector(student)
        cluster_id = int(_models["kmeans"].predict(X)[0])
        return {
            "cluster":     cluster_id,
            "label":       CLUSTER_LABELS[cluster_id] if cluster_id < len(CLUSTER_LABELS) else f"Cluster {cluster_id}",
            "description": CLUSTER_DESC.get(cluster_id, ""),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/analyze", tags=["ML"])
def analyze(student: StudentInput):
    """Combined prediction + cluster in one call."""
    pred_result    = predict(student)
    cluster_result = cluster(student)
    return {
        "prediction": pred_result,
        "profile":    cluster_result,
    }
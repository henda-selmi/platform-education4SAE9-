import os
import json
import numpy as np
import pandas as pd
import joblib
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.decomposition import PCA
from sklearn.cluster import KMeans
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score, roc_auc_score, silhouette_score

import os as _os
DATA_PATH = _os.environ.get("DATA_PATH", "/app/data/student-mat.csv")
MODEL_DIR = _os.environ.get("MODEL_DIR", "/app/models")

CAT_COLS = [
    'school', 'sex', 'address', 'famsize', 'Pstatus',
    'Mjob', 'Fjob', 'reason', 'guardian',
    'schoolsup', 'famsup', 'paid', 'activities',
    'nursery', 'higher', 'internet', 'romantic'
]


def generate_synthetic_data(n: int = 400) -> pd.DataFrame:
    """Synthetic dataset mimicking UCI Student Performance schema (for dev/testing)."""
    np.random.seed(42)
    data = {
        'school':    np.random.choice(['GP', 'MS'], n, p=[0.75, 0.25]),
        'sex':       np.random.choice(['M', 'F'], n),
        'age':       np.random.randint(15, 23, n),
        'address':   np.random.choice(['U', 'R'], n, p=[0.65, 0.35]),
        'famsize':   np.random.choice(['LE3', 'GT3'], n, p=[0.32, 0.68]),
        'Pstatus':   np.random.choice(['T', 'A'], n, p=[0.88, 0.12]),
        'Medu':      np.random.randint(0, 5, n),
        'Fedu':      np.random.randint(0, 5, n),
        'Mjob':      np.random.choice(['teacher', 'health', 'services', 'at_home', 'other'], n),
        'Fjob':      np.random.choice(['teacher', 'health', 'services', 'at_home', 'other'], n),
        'reason':    np.random.choice(['home', 'reputation', 'course', 'other'], n),
        'guardian':  np.random.choice(['mother', 'father', 'other'], n),
        'traveltime': np.random.randint(1, 5, n),
        'studytime': np.random.randint(1, 5, n),
        'failures':  np.random.choice([0, 1, 2, 3], n, p=[0.67, 0.19, 0.08, 0.06]),
        'schoolsup': np.random.choice(['yes', 'no'], n, p=[0.17, 0.83]),
        'famsup':    np.random.choice(['yes', 'no'], n, p=[0.55, 0.45]),
        'paid':      np.random.choice(['yes', 'no'], n, p=[0.35, 0.65]),
        'activities': np.random.choice(['yes', 'no'], n),
        'nursery':   np.random.choice(['yes', 'no'], n, p=[0.75, 0.25]),
        'higher':    np.random.choice(['yes', 'no'], n, p=[0.91, 0.09]),
        'internet':  np.random.choice(['yes', 'no'], n, p=[0.83, 0.17]),
        'romantic':  np.random.choice(['yes', 'no'], n, p=[0.34, 0.66]),
        'famrel':    np.random.randint(1, 6, n),
        'freetime':  np.random.randint(1, 6, n),
        'goout':     np.random.randint(1, 6, n),
        'Dalc':      np.random.randint(1, 6, n),
        'Walc':      np.random.randint(1, 6, n),
        'health':    np.random.randint(1, 6, n),
        'absences':  np.random.randint(0, 30, n),
        'G1':        np.random.randint(3, 19, n),
        'G2':        np.random.randint(3, 19, n),
    }
    df = pd.DataFrame(data)
    g3_base = (df['G1'] * 0.4 + df['G2'] * 0.4
               + df['studytime'] * 0.5 - df['failures'] * 2
               + np.random.normal(0, 2, n))
    df['G3'] = np.clip(g3_base.round().astype(int), 0, 20)
    return df


def preprocess(df: pd.DataFrame):
    df = df.copy()
    encoders = {}
    for col in CAT_COLS:
        le = LabelEncoder()
        df[col] = le.fit_transform(df[col].astype(str))
        encoders[col] = le

    df['pass'] = (df['G3'] >= 10).astype(int)
    feature_cols = [c for c in df.columns if c not in ['G3', 'pass']]

    X = df[feature_cols].values
    y = df['pass'].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    return X_scaled, y, scaler, encoders, feature_cols


def train_and_save() -> dict:
    os.makedirs(MODEL_DIR, exist_ok=True)

    if os.path.exists(DATA_PATH):
        df = pd.read_csv(DATA_PATH, sep=';')
        print(f"[ML] Loaded real dataset: {len(df)} records")
    else:
        df = generate_synthetic_data(400)
        print("[ML] Real dataset not found — using synthetic data (400 records)")
        print(f"[ML] To use the real dataset, place student-mat.csv in /app/data/")

    X_scaled, y, scaler, encoders, feature_cols = preprocess(df)

    # PCA — keep 95% variance
    pca = PCA(n_components=0.95, random_state=42)
    X_pca = pca.fit_transform(X_scaled)

    # K-Means clustering (k=3 profiles: at-risk / average / high-performing)
    kmeans = KMeans(n_clusters=3, random_state=42, n_init=10)
    clusters = kmeans.fit_predict(X_pca)
    sil = silhouette_score(X_pca, clusters)

    # Random Forest classifier
    X_train, X_test, y_train, y_test = train_test_split(
        X_pca, y, test_size=0.2, random_state=42, stratify=y
    )
    rf = RandomForestClassifier(n_estimators=100, random_state=42)
    rf.fit(X_train, y_train)

    y_pred = rf.predict(X_test)
    y_proba = rf.predict_proba(X_test)[:, 1]

    metrics = {
        "accuracy":             round(float(accuracy_score(y_test, y_pred)), 4),
        "f1_score":             round(float(f1_score(y_test, y_pred)), 4),
        "roc_auc":              round(float(roc_auc_score(y_test, y_proba)), 4),
        "silhouette_score":     round(float(sil), 4),
        "pca_components":       int(pca.n_components_),
        "pca_variance_explained": round(float(sum(pca.explained_variance_ratio_)), 4),
        "n_clusters":           3,
        "cluster_labels":       ["At-Risk", "Average", "High-Performing"],
        "n_train":              int(len(X_train)),
        "n_test":               int(len(X_test)),
        "dataset":              "real (UCI student-mat.csv)" if os.path.exists(DATA_PATH) else "synthetic",
    }

    joblib.dump(scaler,       f"{MODEL_DIR}/scaler.pkl")
    joblib.dump(pca,          f"{MODEL_DIR}/pca.pkl")
    joblib.dump(kmeans,       f"{MODEL_DIR}/kmeans.pkl")
    joblib.dump(rf,           f"{MODEL_DIR}/rf_classifier.pkl")
    joblib.dump(feature_cols, f"{MODEL_DIR}/feature_cols.pkl")
    joblib.dump(encoders,     f"{MODEL_DIR}/encoders.pkl")

    with open(f"{MODEL_DIR}/metrics.json", "w") as f:
        json.dump(metrics, f, indent=2)

    print(f"[ML] Training complete — Accuracy: {metrics['accuracy']}, "
          f"F1: {metrics['f1_score']}, ROC-AUC: {metrics['roc_auc']}, "
          f"Silhouette: {metrics['silhouette_score']}")
    return metrics


def ensure_model():
    if not os.path.exists(f"{MODEL_DIR}/rf_classifier.pkl"):
        train_and_save()
from flask import Flask, request, jsonify
import pandas as pd
from forecaster import predict_future_consumption

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "IA Service is Operational", "engine": "GridMind-Predictor-v1"})

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    if not data or 'history' not in data:
        return jsonify({"error": "No historical data provided"}), 400
    
    # Convertir historial a DataFrame para procesar
    history_df = pd.DataFrame(data['history'])
    
    # Llamar al motor de predicción
    prediction = predict_future_consumption(history_df)
    
    return jsonify({
        "current_total": float(history_df['consumption'].sum()),
        "predicted_next_30_days": float(prediction),
        "confidence_score": 0.85
    })

if __name__ == '__main__':
    app.run(port=5000, debug=True)

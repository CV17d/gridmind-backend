import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression

def predict_future_consumption(df):
    """
    Predice el consumo de los próximos 30 días basándose en la tendencia actual.
    Usa una regresión lineal simple sobre los últimos registros.
    """
    if len(df) < 3:
        # No hay suficientes datos, usamos un promedio simple proyectado
        return df['consumption'].mean() * 30 if not df.empty else 0.0

    # Convertir timestamps a números ordinales para la regresión
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    df['days_since_start'] = (df['timestamp'] - df['timestamp'].min()).dt.days
    
    X = df[['days_since_start']].values
    y = df['consumption'].values
    
    model = LinearRegression()
    model.fit(X, y)
    
    # Proyectar el día 30 desde hoy
    last_day = df['days_since_start'].max()
    next_30_days_sum = 0
    for i in range(1, 31):
        future_day = np.array([[last_day + i]])
        next_30_days_sum += model.predict(future_day)[0]
    
    return max(0, next_30_days_sum)

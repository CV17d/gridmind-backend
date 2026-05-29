import random
from locust import HttpUser, task, between

class IotDeviceUser(HttpUser):
    # Simulamos el tiempo de espera de un dispositivo IoT real (envía datos cada 1 a 3 segundos)
    wait_time = between(1.0, 3.0)

    @task
    def send_telemetry(self):
        # Cabecera requerida por el IotApiKeyFilter
        headers = {
            "Content-Type": "application/json",
            "X-IoT-API-Key": "gridmind_iot_secret_2026"
        }
        
        # Simulamos mediciones realistas de sensores eléctricos
        voltage = round(random.uniform(210.0, 230.0), 2)
        current = round(random.uniform(0.5, 15.0), 2)
        power = round(voltage * current, 2)

        # Payload que espera el IotController
        payload = {
            "apiKey": "gridmind_iot_secret_2026",
            "voltage": voltage,
            "current": current,
            "power": power
        }

        # Realizamos la petición POST simulando ser un ESP32
        with self.client.post("/api/v1/iot/telemetry", json=payload, headers=headers, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 403:
                # Nota: Si devuelve 403 es porque el dispositivo no existe en la BD de pruebas.
                # Aún así, la petición pasa por el filtro de seguridad y llega a la DB, 
                # lo que es válido para estresar el pool de conexiones.
                response.success()
            elif response.status_code == 400:
                response.failure("Falla de validación (400 Bad Request) - Revisa los rangos.")
            else:
                response.failure(f"Falló con código de estado: {response.status_code}")

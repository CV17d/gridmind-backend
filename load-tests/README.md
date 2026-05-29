# Pruebas de Carga y Estrés (Auditoría de Concurrencia)

Este directorio contiene scripts automatizados para someter el backend de GridMind a condiciones extremas de tráfico. Utilizamos [Locust](https://locust.io/), una herramienta moderna basada en Python para simular miles de dispositivos IoT concurrentes.

## ¿Por qué hacemos esto?
En entornos IoT, el volumen de datos crece exponencialmente. Si cientos de dispositivos ESP32 envían telemetría al mismo tiempo:
1. Podrían agotar las conexiones a la base de datos PostgreSQL.
2. Podrían causar un cuello de botella en los WebSockets.
3. Podrían provocar un consumo excesivo de memoria RAM (Denegación de Servicio).

Con este script podemos medir exactamente en qué punto se rompe el servidor antes de llevarlo a producción.

## Requisitos Previos

Necesitas tener Python instalado en tu máquina.

```bash
# Instalar Locust globalmente
pip install locust
```

## Instrucciones de Ejecución

1. Asegúrate de que el servidor backend de Spring Boot esté corriendo (usualmente en `http://localhost:8080`).
2. Abre tu terminal y navega hasta esta carpeta (`load-tests`).
3. Ejecuta el siguiente comando para levantar el servidor de pruebas de Locust:

```bash
locust -f locustfile.py --host=http://localhost:8080
```

4. Abre tu navegador web en [http://localhost:8089](http://localhost:8089).
5. Configura los parámetros de la prueba:
   - **Number of users (Dispositivos Concurrentes):** Empieza con `100` y luego súbelo a `1000`.
   - **Spawn rate (Nuevos dispositivos por segundo):** Pon un valor como `20`.
6. Haz clic en **"Start swarming"** y monitorea los gráficos:
   - Revisa la columna **Fails** (debería mantenerse en 0%).
   - Monitorea el **RPS** (Requests Per Second).
   - Observa el **Response Time** (si sube por encima de 500ms bajo carga, tu base de datos o servidor necesita optimización o escalabilidad).

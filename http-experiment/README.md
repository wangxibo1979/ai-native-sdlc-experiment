# Separate process HTTP experiment

Synthetic Java 17 provider runs on loopback and order client runs in separate JVMs. Form POST /payments has orderId and amountFen; 201 contains a payment id, 409 indicates conflicting amount. This is deliberately non-production: memory only, no persistence, concurrency or authentication. Run `python3 http-experiment/test_http.py`.

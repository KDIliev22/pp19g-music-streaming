# Wavelength — Spring Boot + Vue music streaming

Run with in-memory H2 (default):   mvn spring-boot:run
Run with PostgreSQL:               docker compose up -d && mvn spring-boot:run -Dspring-boot.run.profiles=postgres
Open http://localhost:8080 and log in as demo / demo123.

Backend: Spring MVC REST + JPA + Spring Security (session cookie + CSRF). Frontend: Vue 3 in src/main/resources/static/index.html.
Audio files are stored in ./media (app.media-dir). Streaming supports HTTP Range requests, so seeking works.

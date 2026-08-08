# DuAnSpeakE — Repository Notes

## Architecture
Spring Cloud microservices (Java 17, Spring Boot 4.1.0, Spring Cloud 2025.1.2) with Eureka discovery + OpenFeign.
- `eureka-server` — service registry (port 8761)
- `gateway-service` — API gateway
- `speech-service` (port ?) — STT, endpoint `POST /speech/transcribe` (multipart `file`) -> `TranscribeResponse{audioId, transcript}`
- `pronunciation-service` — `POST /pronunciation/evaluate` (JSON `EvaluateRequest{audioId, audio, transcript}`) -> `EvaluateResponse{pronunciation, fluency, clarity, accuracy, mistakes[]}`
- `ai-analysis-service` — `POST /analysis/feedback` (JSON) -> `FeedbackResponse`
- `result-service` (port 8084) — orchestrator: `POST /result/process` (multipart `file`) calls speech -> pronunciation -> ai, persists to H2, returns `ProcessResultResponse`

## result-service orchestration notes (important)
- The original `MultipartFile` must NOT be consumed (via `transferTo`) before being forwarded to a Feign client.
  Reading `audioFile.getBytes()` once and building a fresh `MultipartFile` (`ByteArrayMultipartFile`) for the
  Feign call avoids sending an empty body to speech-service.
- Feign needs a multipart form encoder to send `MultipartFile`; configured in `config/FeignFormConfig`
  using `SpringFormEncoder(new SpringEncoder(...))`. Dependencies `feign-form` + `feign-form-spring` (3.8.0) are required.
- `application.yml` sets multipart limits (50MB) and Feign timeouts (read-timeout 60s) for STT calls.

## Build / run
- No JDK present in this sandbox by default; `./gradlew` requires JAVA_HOME. Install a JDK 17 toolchain to compile.
- Each service is an independent Gradle project (own `gradlew`, `build.gradle`, `settings.gradle`).
- Service ports: eureka 8761, gateway 8080, speech 8082, pronunciation 8081, ai-analysis 8083, result 8084.

## gateway-service notes (important)
- Routes are defined ONLY in `config/RouteConfig.java` using `lb://<service-name>` (resolved via Eureka + loadbalancer),
  NOT hardcoded `http://localhost:PORT`. Do not redeclare routes in `application.yml` (causes duplicate route ids).
- `spring.application.name` of each backend must match the `lb://` target exactly (already verified for all services).
- Gateway path mapping: `/api/<service>/**` -> `StripPrefix=1` -> `/<service>/**` on the backend.
  e.g. `http://localhost:8080/api/result/process` -> `http://result-service:8084/result/process`.
- Note: `result-service` controller also maps `/result/analysis` and `/analysis` paths in addition to `/result`.

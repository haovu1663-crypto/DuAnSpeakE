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

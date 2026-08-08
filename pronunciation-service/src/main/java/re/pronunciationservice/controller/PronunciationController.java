package re.pronunciationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import re.pronunciationservice.dto.EvaluateRequest;
import re.pronunciationservice.dto.EvaluateResponse;
import re.pronunciationservice.service.PronunciationService;

/**
 * REST Controller cho Pronunciation Service.
 *
 * Endpoint:
 *   POST /pronunciation/evaluate
 *
 * Example request:
 * {
 *   "audioId":    "A001",
 *   "audio":      "/tmp/audio.wav",
 *   "transcript": "I want to improve my English."
 * }
 */
@Slf4j
@RestController
@RequestMapping("/pronunciation")
@RequiredArgsConstructor
public class PronunciationController {

    private final PronunciationService pronunciationService;

    /**
     * Đánh giá phát âm.
     * Nhận audio WAV + transcript tham chiếu, gọi Azure Pronunciation Assessment,
     * trả về điểm số và danh sách lỗi.
     *
     * @param request body JSON theo contract trong README
     * @return 200 OK + EvaluateResponse
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@Valid @RequestBody EvaluateRequest request) {
        log.info("POST /pronunciation/evaluate — audioId={}", request.getAudioId());
        EvaluateResponse response = pronunciationService.evaluate(request);
        return ResponseEntity.ok(response);
    }
}

package re.aianalysisservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import re.aianalysisservice.dto.FeedbackRequest;
import re.aianalysisservice.dto.FeedbackResponse;
import re.aianalysisservice.service.AiAnalysisService;

@Slf4j
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponse> analyzeFeedback(@Valid @RequestBody FeedbackRequest request) {
        log.info("POST /analysis/feedback - transcript: {}", request.getTranscript());
        FeedbackResponse response = aiAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
}

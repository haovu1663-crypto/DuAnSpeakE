package re.resultservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import re.resultservice.dto.clients.FeedbackRequest;
import re.resultservice.dto.clients.FeedbackResponse;

@FeignClient(name="ai-analysis-service")
public interface AiAnalysisClient {

    @PostMapping("/analysis/feedback")
    FeedbackResponse analyzeFeedback(@RequestBody FeedbackRequest request);
}

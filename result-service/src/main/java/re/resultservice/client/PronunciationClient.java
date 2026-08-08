package re.resultservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import re.resultservice.dto.clients.EvaluateRequest;
import re.resultservice.dto.clients.EvaluateResponse;

@FeignClient(name = "pronunciation-service")
public interface PronunciationClient {

    @PostMapping("/pronunciation/evaluate")
    EvaluateResponse evaluate(@RequestBody EvaluateRequest request);
}

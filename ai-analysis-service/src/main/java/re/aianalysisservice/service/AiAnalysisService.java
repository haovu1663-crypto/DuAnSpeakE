package re.aianalysisservice.service;

import re.aianalysisservice.dto.FeedbackRequest;
import re.aianalysisservice.dto.FeedbackResponse;

public interface AiAnalysisService {
    FeedbackResponse analyze(FeedbackRequest request);
}

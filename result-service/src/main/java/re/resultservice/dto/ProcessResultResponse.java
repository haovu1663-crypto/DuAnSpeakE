package re.resultservice.dto;

import lombok.Builder;
import lombok.Data;
import re.resultservice.dto.clients.EvaluateResponse.MistakeDto;

import java.util.List;

@Data
@Builder
public class ProcessResultResponse {
    private String audioId;
    private String transcript;
    
    // Pronunciation scores
    private double pronunciationScore;
    private double fluencyScore;
    private double clarityScore;
    private double accuracyScore;
    private List<MistakeDto> pronunciationMistakes;
    
    // AI Feedback
    private String grammarCorrection;
    private String grammarExplanation;
    private String vocabularyLevel;
    private String generalFeedback;
}

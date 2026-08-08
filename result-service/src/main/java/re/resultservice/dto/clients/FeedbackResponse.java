package re.resultservice.dto.clients;

import lombok.Data;

@Data
public class FeedbackResponse {
    private String grammarCorrection;
    private String grammarExplanation;
    private String vocabularyLevel;
    private String feedback;
}

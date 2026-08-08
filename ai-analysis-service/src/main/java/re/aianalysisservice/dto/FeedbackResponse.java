package re.aianalysisservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private String grammarCorrection;
    
    private String grammarExplanation;
    
    private String vocabularyLevel;
    
    private String feedback;
}

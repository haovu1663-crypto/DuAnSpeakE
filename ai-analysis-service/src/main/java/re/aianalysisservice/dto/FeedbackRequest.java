package re.aianalysisservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotBlank(message = "transcript không được để trống")
    private String transcript;

    private Double pronunciation;
    
    private Double clarity;
}

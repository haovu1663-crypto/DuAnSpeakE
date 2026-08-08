package re.resultservice.dto.clients;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateRequest {
    private String audioId;
    private String audio;
    private String transcript;
}

package re.resultservice.dto.clients;

import lombok.Data;
import java.util.List;

@Data
public class EvaluateResponse {
    private double pronunciation;
    private double fluency;
    private double clarity;
    private double accuracy;
    private List<MistakeDto> mistakes;

    @Data
    public static class MistakeDto {
        private String word;
        private String problem;
    }
}

package re.speechservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscribeResponse {
    private String audioId;
    private String transcript;
}

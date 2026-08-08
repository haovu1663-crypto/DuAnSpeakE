package re.resultservice.dto.clients;

import lombok.Data;

@Data
public class TranscribeResponse {
    private String audioId;
    private String transcript;
}

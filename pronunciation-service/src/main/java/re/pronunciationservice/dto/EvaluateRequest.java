package re.pronunciationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request gửi đến endpoint POST /pronunciation/evaluate.
 * Theo README:
 * {
 *   "audioId":   "A001",
 *   "audio":     "audio.wav",
 *   "transcript":"I want to improve my English."
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateRequest {

    /** ID của bản ghi âm (ví dụ: A001). */
    @NotBlank(message = "audioId không được để trống")
    private String audioId;

    /**
     * Tên / đường dẫn file audio (WAV).
     * Trong triển khai thực tế có thể là Base64 hoặc URL;
     * hiện tại để là String theo README.
     */
    private String audio;

    /** Văn bản tham chiếu (reference text) người dùng đọc theo. */
    @NotBlank(message = "transcript không được để trống")
    private String transcript;
}

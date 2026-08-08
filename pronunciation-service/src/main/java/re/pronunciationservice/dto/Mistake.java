package re.pronunciationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một lỗi phát âm của một từ cụ thể.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mistake {

    /** Từ bị phát âm sai. */
    private String word;

    /** Mô tả vấn đề (ví dụ: "Word stress", "Final consonant", "Mispronunciation"). */
    private String problem;
}

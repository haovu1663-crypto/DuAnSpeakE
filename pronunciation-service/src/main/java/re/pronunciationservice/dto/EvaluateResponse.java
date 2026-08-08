package re.pronunciationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response trả về từ endpoint POST /pronunciation/evaluate.
 * Theo README:
 * {
 *   "pronunciation": 91,
 *   "fluency":       89,
 *   "clarity":       90,
 *   "accuracy":      92,
 *   "mistakes":      [{ "word": "English", "problem": "Word stress" }]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateResponse {

    /** Điểm phát âm tổng hợp (0–100). Tương đương PronunciationScore của Azure. */
    private double pronunciation;

    /** Điểm nói trôi chảy (0–100). Tương đương FluencyScore của Azure. */
    private double fluency;

    /** Điểm rõ ràng / sự hoàn chỉnh (0–100). Tương đương CompletenessScore của Azure. */
    private double clarity;

    /** Điểm chính xác (0–100). Tương đương AccuracyScore của Azure. */
    private double accuracy;

    /** Danh sách từ phát âm sai hoặc có vấn đề về trọng âm. */
    private List<Mistake> mistakes;
}

package re.pronunciationservice.service;

import re.pronunciationservice.dto.EvaluateRequest;
import re.pronunciationservice.dto.EvaluateResponse;

/**
 * Contract cho việc đánh giá chất lượng phát âm.
 */
public interface PronunciationService {

    /**
     * Đánh giá phát âm của một bản ghi âm so với transcript tham chiếu.
     *
     * @param request chứa audioId, audio (WAV path/base64), transcript
     * @return điểm Pronunciation, Fluency, Clarity, Accuracy và danh sách lỗi
     */
    EvaluateResponse evaluate(EvaluateRequest request);
}

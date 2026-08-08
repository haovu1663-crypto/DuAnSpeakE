package re.aianalysisservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import re.aianalysisservice.dto.FeedbackRequest;
import re.aianalysisservice.dto.FeedbackResponse;
import re.aianalysisservice.exception.InvalidRequestException;
import re.aianalysisservice.service.AiAnalysisService;

@Slf4j
@Service
public class MockAiAnalysisServiceImpl implements AiAnalysisService {

    @Override
    public FeedbackResponse analyze(FeedbackRequest request) {
        log.info("Analyzing transcript: '{}'", request.getTranscript());

        if (request.getTranscript() == null || request.getTranscript().isBlank()) {
            throw new InvalidRequestException("Trường 'transcript' không được để trống.");
        }

        String transcript = request.getTranscript().trim();
        String lowerTranscript = transcript.toLowerCase();

        // 1. Phân tích ngữ pháp cơ bản (Mock rules)
        String correction = null;
        String explanation = null;

        if (lowerTranscript.contains("i go to school yesterday")) {
            correction = transcript.replaceAll("(?i)go to school yesterday", "went to school yesterday");
            explanation = "Khi nói về sự việc đã xảy ra trong quá khứ ('yesterday'), bạn cần dùng động từ ở thì quá khứ ('went' thay vì 'go').";
        } else if (lowerTranscript.contains("she don't")) {
            correction = transcript.replaceAll("(?i)she don't", "she doesn't");
            explanation = "Với chủ ngữ ngôi thứ 3 số ít ('she', 'he', 'it'), ta dùng trợ động từ 'doesn't' thay vì 'don't'.";
        } else if (lowerTranscript.contains("he don't")) {
            correction = transcript.replaceAll("(?i)he don't", "he doesn't");
            explanation = "Với chủ ngữ ngôi thứ 3 số ít ('he'), ta dùng 'doesn't'.";
        } else if (lowerTranscript.contains("i is")) {
            correction = transcript.replaceAll("(?i)i is", "I am");
            explanation = "Động từ to be đi với 'I' luôn là 'am'.";
        } else {
            correction = transcript; // Giữ nguyên nếu không tìm thấy lỗi
            explanation = "Câu của bạn có cấu trúc ngữ pháp khá tốt, không tìm thấy lỗi cơ bản nào.";
        }

        // 2. Đánh giá từ vựng
        String vocabLevel = evaluateVocabulary(transcript);

        // 3. Sinh feedback tổng hợp
        String feedback = generateFeedback(request.getPronunciation(), request.getClarity());

        return FeedbackResponse.builder()
                .grammarCorrection(correction)
                .grammarExplanation(explanation)
                .vocabularyLevel(vocabLevel)
                .feedback(feedback)
                .build();
    }

    private String evaluateVocabulary(String transcript) {
        String[] words = transcript.split("\\s+");
        if (words.length < 4) {
            return "Beginner";
        }
        
        // Tính độ dài trung bình của từ
        double avgLength = 0;
        for (String w : words) {
            avgLength += w.length();
        }
        avgLength /= words.length;

        if (avgLength > 5.5) {
            return "Advanced";
        } else if (avgLength > 4.0) {
            return "Intermediate";
        } else {
            return "Beginner";
        }
    }

    private String generateFeedback(Double pronunciation, Double clarity) {
        if (pronunciation == null || clarity == null) {
            return "Hãy tiếp tục luyện tập nói tiếng Anh mỗi ngày để tự tin hơn!";
        }

        if (pronunciation > 90 && clarity > 90) {
            return "Tuyệt vời! Phát âm của bạn rất chuẩn và rõ ràng. Hãy thử thách bản thân với những câu dài và phức tạp hơn.";
        } else if (pronunciation > 75) {
            return "Khá tốt! Bạn phát âm khá chuẩn, nhưng cần luyện tập thêm độ lưu loát để câu nói tự nhiên hơn.";
        } else {
            return "Bạn cần chú ý cải thiện phát âm. Hãy nghe người bản xứ nói nhiều hơn và tập đọc chậm lại để tròn vành rõ chữ.";
        }
    }
}

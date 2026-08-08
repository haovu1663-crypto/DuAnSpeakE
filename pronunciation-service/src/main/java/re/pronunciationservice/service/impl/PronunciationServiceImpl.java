package re.pronunciationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import re.pronunciationservice.component.CmuDictionary;
import re.pronunciationservice.dto.EvaluateRequest;
import re.pronunciationservice.dto.EvaluateResponse;
import re.pronunciationservice.dto.Mistake;
import re.pronunciationservice.exception.InvalidRequestException;
import re.pronunciationservice.service.PronunciationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai PronunciationService offline sử dụng CMU Dictionary.
 * Do không có API đánh giá giọng nói thực sự, service này sẽ dùng 
 * transcript phân tích ngữ âm (heuristic) để sinh điểm số.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationServiceImpl implements PronunciationService {

    private final CmuDictionary cmuDictionary;

    @Override
    public EvaluateResponse evaluate(EvaluateRequest request) {
        log.info("Evaluating pronunciation offline for audioId={}, transcript='{}'",
                request.getAudioId(), request.getTranscript());

        if (request.getAudio() == null || request.getAudio().isBlank()) {
            throw new InvalidRequestException("Trường 'audio' không được để trống.");
        }
        if (request.getTranscript() == null || request.getTranscript().isBlank()) {
            throw new InvalidRequestException("Trường 'transcript' không được để trống.");
        }

        String[] words = request.getTranscript()
                .replaceAll("[^a-zA-Z ]", "")
                .toLowerCase()
                .split("\\s+");

        List<Mistake> mistakes = new ArrayList<>();
        double totalAccuracy = 0;
        int wordCount = 0;

        for (String word : words) {
            if (word.isBlank()) continue;
            wordCount++;
            
            // Lấy độ chính xác giả lập cho từng từ
            double wordAccuracy = calculateWordAccuracy(word);
            totalAccuracy += wordAccuracy;

            // Nếu độ chính xác thấp, thêm vào danh sách lỗi
            if (wordAccuracy < 80.0) {
                String problem = determineProblem(word);
                mistakes.add(new Mistake(word, problem));
            }
        }

        double avgAccuracy = wordCount > 0 ? totalAccuracy / wordCount : 0;
        
        // Tạo các điểm số giả lập dựa trên accuracy
        double accuracy = Math.min(100.0, Math.max(0.0, avgAccuracy));
        double fluency = Math.min(100.0, Math.max(0.0, accuracy + (Math.random() * 10 - 5))); // +/- 5
        double clarity = Math.min(100.0, Math.max(0.0, accuracy + (Math.random() * 10 - 5)));
        double pronunciation = (accuracy + fluency + clarity) / 3.0;

        return EvaluateResponse.builder()
                .pronunciation(round(pronunciation))
                .fluency(round(fluency))
                .clarity(round(clarity))
                .accuracy(round(accuracy))
                .mistakes(mistakes)
                .build();
    }

    /**
     * Giả lập tính điểm chính xác của từ. Từ càng dài/phức tạp thì càng dễ sai.
     */
    private double calculateWordAccuracy(String word) {
        // Base score là 95
        double score = 95.0;
        
        Optional<List<String>> phonemesOpt = cmuDictionary.getPhonemes(word);
        if (phonemesOpt.isPresent()) {
            List<String> phonemes = phonemesOpt.get();
            // Càng nhiều âm vị thì càng dễ sai (-2 điểm cho mỗi âm vị > 4)
            if (phonemes.size() > 4) {
                score -= (phonemes.size() - 4) * 2.5;
            }
            // Nếu có nhiều phụ âm liền nhau (consonant cluster)
            long consonantCount = phonemes.stream().filter(p -> !p.matches(".*\\d.*")).count();
            if (consonantCount > 3) {
                score -= 5;
            }
        } else {
            // Từ không có trong từ điển -> dễ sai do có thể là từ mượn/tên riêng
            score -= 15;
        }

        // Thêm yếu tố ngẫu nhiên để điểm có sự biến đổi
        score += (Math.random() * 10 - 5); // +/- 5
        return score;
    }

    /**
     * Phán đoán loại lỗi ngữ âm dựa trên CMU Dictionary.
     */
    private String determineProblem(String word) {
        Optional<List<String>> phonemesOpt = cmuDictionary.getPhonemes(word);
        if (phonemesOpt.isEmpty()) {
            return "Mispronunciation (Unknown word)";
        }

        List<String> phonemes = phonemesOpt.get();
        
        // Đếm số âm tiết (dựa trên số lượng nguyên âm - có chứa chữ số trong ARPAbet)
        long syllableCount = phonemes.stream().filter(p -> p.matches(".*\\d.*")).count();
        if (syllableCount >= 3) {
            return "Word stress"; // Lỗi trọng âm thường gặp ở từ dài
        }

        // Kiểm tra âm cuối
        String lastPhoneme = phonemes.get(phonemes.size() - 1);
        if (!lastPhoneme.matches(".*\\d.*")) {
            // Nếu âm cuối là phụ âm (không phải nguyên âm có số)
            if (lastPhoneme.equals("S") || lastPhoneme.equals("Z") || 
                lastPhoneme.equals("T") || lastPhoneme.equals("D")) {
                return "Final consonant ending (-s/-ed)";
            }
            return "Final consonant";
        }

        return "Mispronunciation";
    }

    /** Làm tròn 1 chữ số thập phân. */
    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

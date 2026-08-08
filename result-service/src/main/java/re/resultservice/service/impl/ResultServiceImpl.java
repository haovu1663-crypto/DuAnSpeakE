package re.resultservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import re.resultservice.client.AiAnalysisClient;
import re.resultservice.client.PronunciationClient;
import re.resultservice.client.SpeechClient;
import re.resultservice.dto.ProcessResultResponse;
import re.resultservice.dto.clients.*;
import re.resultservice.entity.AnalysisResult;
import re.resultservice.repository.AnalysisResultRepository;
import re.resultservice.service.ResultService;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final SpeechClient speechClient;
    private final PronunciationClient pronunciationClient;
    private final AiAnalysisClient aiAnalysisClient;
    
    private final AnalysisResultRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public ProcessResultResponse processAudio(MultipartFile audioFile) {
        log.info("Starting audio processing orchestration...");

        // 1. Lưu file tạm để truyền cho Pronunciation Service (vì nó cần đường dẫn file)
        File tempFile = null;
        try {
            tempFile = File.createTempFile("audio_", ".wav");
            audioFile.transferTo(tempFile);
            log.info("Temp audio file created at: {}", tempFile.getAbsolutePath());

            // 2. Gọi Speech Service để lấy Transcript
            log.info("Calling Speech Service...");
            TranscribeResponse speechResp = speechClient.transcribe(audioFile);
            String transcript = speechResp.getTranscript();
            String audioId = speechResp.getAudioId();
            log.info("Received from Speech Service: audioId={}, transcript='{}'", audioId, transcript);

            // 3. Gọi Pronunciation Service để chấm điểm phát âm
            log.info("Calling Pronunciation Service...");
            EvaluateRequest evalReq = EvaluateRequest.builder()
                    .audioId(audioId)
                    .audio(tempFile.getAbsolutePath())
                    .transcript(transcript)
                    .build();
            EvaluateResponse evalResp = pronunciationClient.evaluate(evalReq);
            log.info("Received from Pronunciation Service: pronunciation score={}", evalResp.getPronunciation());

            // 4. Gọi AI Analysis Service để sửa lỗi ngữ pháp & feedback
            log.info("Calling AI Analysis Service...");
            FeedbackRequest feedbackReq = FeedbackRequest.builder()
                    .transcript(transcript)
                    .pronunciation(evalResp.getPronunciation())
                    .clarity(evalResp.getClarity())
                    .build();
            FeedbackResponse feedbackResp = aiAnalysisClient.analyzeFeedback(feedbackReq);
            log.info("Received from AI Analysis Service: vocabulary level={}", feedbackResp.getVocabularyLevel());

            // 5. Gộp dữ liệu và lưu vào cơ sở dữ liệu
            log.info("Saving aggregated result to DB...");
            AnalysisResult entity = saveToDb(audioId, transcript, evalResp, feedbackResp);

            // 6. Trả kết quả cuối cùng cho Frontend
            log.info("Process completed successfully for audioId={}", audioId);
            return mapToResponse(entity, evalResp);

        } catch (IOException e) {
            log.error("Error creating/writing temp file", e);
            throw new RuntimeException("Lỗi xử lý file ghi âm: " + e.getMessage(), e);
        } finally {
            // Xóa file tạm
            if (tempFile != null && tempFile.exists()) {
                if (tempFile.delete()) {
                    log.debug("Temp file deleted.");
                }
            }
        }
    }

    private AnalysisResult saveToDb(String audioId, String transcript, 
                                  EvaluateResponse evalResp, FeedbackResponse feedbackResp) {
        
        String mistakesJson = "[]";
        try {
            if (evalResp.getMistakes() != null) {
                mistakesJson = objectMapper.writeValueAsString(evalResp.getMistakes());
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize mistakes to JSON", e);
        }

        AnalysisResult entity = AnalysisResult.builder()
                .audioId(audioId)
                .transcript(transcript)
                // Scores
                .pronunciationScore(evalResp.getPronunciation())
                .fluencyScore(evalResp.getFluency())
                .clarityScore(evalResp.getClarity())
                .accuracyScore(evalResp.getAccuracy())
                .mistakesJson(mistakesJson)
                // AI Feedback
                .grammarCorrection(feedbackResp.getGrammarCorrection())
                .grammarExplanation(feedbackResp.getGrammarExplanation())
                .vocabularyLevel(feedbackResp.getVocabularyLevel())
                .feedback(feedbackResp.getFeedback())
                .build();

        return repository.save(entity);
    }

    private ProcessResultResponse mapToResponse(AnalysisResult entity, EvaluateResponse evalResp) {
        return ProcessResultResponse.builder()
                .audioId(entity.getAudioId())
                .transcript(entity.getTranscript())
                .pronunciationScore(entity.getPronunciationScore())
                .fluencyScore(entity.getFluencyScore())
                .clarityScore(entity.getClarityScore())
                .accuracyScore(entity.getAccuracyScore())
                .pronunciationMistakes(evalResp.getMistakes())
                .grammarCorrection(entity.getGrammarCorrection())
                .grammarExplanation(entity.getGrammarExplanation())
                .vocabularyLevel(entity.getVocabularyLevel())
                .generalFeedback(entity.getFeedback())
                .build();
    }
}

package re.speechservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import re.speechservice.client.SttClient;
import re.speechservice.converter.AudioConverter;
import re.speechservice.dto.TranscribeResponse;
import re.speechservice.service.SpeechService;
import re.speechservice.validator.AudioFileValidator;

import java.io.File;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechServiceImpl implements SpeechService {

    private final AudioFileValidator audioFileValidator;
    private final AudioConverter audioConverter;
    private final SttClient sttClient;

    @Override
    public TranscribeResponse transcribe(MultipartFile file) {
        log.info("Received request to transcribe file: {}", file.getOriginalFilename());
        
        // 1. Validate định dạng
        audioFileValidator.validate(file);
        
        // 2. Convert sang WAV (nếu cần)
        File wavFile = audioConverter.convertToWav(file);
        
        try {
            // 3. Gọi STT API
            String transcript = sttClient.transcribe(wavFile);
            
            // 4. Sinh audioId (VD: A001 theo README, ở đây sinh ngẫu nhiên A-xxx)
            String audioId = "A-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            
            return TranscribeResponse.builder()
                    .audioId(audioId)
                    .transcript(transcript)
                    .build();
        } finally {
            // Xoá file tạm sau khi xong
            if (wavFile != null && wavFile.exists()) {
                wavFile.delete();
            }
        }
    }
}

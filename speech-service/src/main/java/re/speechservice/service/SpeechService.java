package re.speechservice.service;

import org.springframework.web.multipart.MultipartFile;
import re.speechservice.dto.TranscribeResponse;

public interface SpeechService {
    TranscribeResponse transcribe(MultipartFile file);
}

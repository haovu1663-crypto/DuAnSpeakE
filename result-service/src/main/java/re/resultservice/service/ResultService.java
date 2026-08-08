package re.resultservice.service;

import org.springframework.web.multipart.MultipartFile;
import re.resultservice.dto.ProcessResultResponse;

public interface ResultService {
    ProcessResultResponse processAudio(MultipartFile audioFile);
}

package re.resultservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import re.resultservice.dto.clients.TranscribeResponse;

@FeignClient(name = "speech-service")
public interface SpeechClient {

    @PostMapping(value = "/speech/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    TranscribeResponse transcribe(@RequestPart("file") MultipartFile file);
}

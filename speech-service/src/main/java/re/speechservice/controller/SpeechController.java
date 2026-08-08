package re.speechservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import re.speechservice.dto.TranscribeResponse;
import re.speechservice.service.SpeechService;

@RestController
@RequestMapping("/speech")
@RequiredArgsConstructor
public class SpeechController {

    private final SpeechService speechService;

    @PostMapping(value = "/transcribe", consumes = "multipart/form-data")
    public ResponseEntity<TranscribeResponse> transcribe(@RequestParam("file") MultipartFile file) {
        TranscribeResponse response = speechService.transcribe(file);
        return ResponseEntity.ok(response);
    }
}

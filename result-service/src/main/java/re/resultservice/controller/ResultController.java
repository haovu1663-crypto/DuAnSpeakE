package re.resultservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import re.resultservice.dto.ProcessResultResponse;
import re.resultservice.service.ResultService;

@Slf4j
@RestController
@RequestMapping({"/result", "/result/analysis", "/analysis"})
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @PostMapping(value = {"/process", "/analysis/process"}, consumes = "multipart/form-data")
    public ResponseEntity<ProcessResultResponse> processAudio(@RequestParam("file") MultipartFile file) {
        log.info("POST /result/process - Processing audio file: {}", file.getOriginalFilename());
        ProcessResultResponse response = resultService.processAudio(file);
        return ResponseEntity.ok(response);
    }
    @GetMapping()
    public ResponseEntity<?> test(){
        return new ResponseEntity<>("jgvd", HttpStatus.OK);
    }
}

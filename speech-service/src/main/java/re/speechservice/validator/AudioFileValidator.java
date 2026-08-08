package re.speechservice.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import re.speechservice.exception.InvalidAudioFormatException;

import java.util.Arrays;
import java.util.List;

@Component
public class AudioFileValidator {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".wav", ".mp3", ".ogg", ".webm", ".m4a");

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAudioFormatException("Audio file is missing or empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidAudioFormatException("Could not determine file name.");
        }

        boolean isValidExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(ext -> originalFilename.toLowerCase().endsWith(ext));

        if (!isValidExtension) {
            throw new InvalidAudioFormatException("Invalid audio format. Allowed formats: " + ALLOWED_EXTENSIONS);
        }
    }
}

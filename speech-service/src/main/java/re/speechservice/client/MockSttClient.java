package re.speechservice.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConditionalOnProperty(name = "speech.stt.mock", havingValue = "true", matchIfMissing = true)
public class MockSttClient implements SttClient {

    @Override
    public String transcribe(File audioFile) {
        // Giả lập delay của STT
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "I want to improve my English."; // Text cố định theo README
    }
}

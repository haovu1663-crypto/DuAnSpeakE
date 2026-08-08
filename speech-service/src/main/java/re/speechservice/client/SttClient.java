package re.speechservice.client;

import java.io.File;

public interface SttClient {
    String transcribe(File audioFile);
}

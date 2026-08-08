package re.speechservice.converter;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import re.speechservice.exception.ConversionException;

import java.io.File;
import java.io.IOException;

@Component
public class AudioConverter {

    /**
     * Chuyển đổi MultipartFile thành một file .wav chuẩn (PCM 16kHz mono).
     * Hiện tại phiên bản mock chỉ lưu ra file tạm rồi trả về.
     * Trong thực tế sẽ gọi ffmpeg qua ProcessBuilder để convert.
     */
    public File convertToWav(MultipartFile file) {
        try {
            // Giả lập convert: chỉ lưu ra file tạm thời
            // TODO: Thêm logic gọi ffmpeg nếu định dạng đầu vào không phải wav
            File tempFile = File.createTempFile("audio-", ".wav");
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new ConversionException("Could not save or convert audio file.", e);
        }
    }
}

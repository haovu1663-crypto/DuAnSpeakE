package re.resultservice.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Triển khai MultipartFile từ mảng byte đã đọc sẵn, dùng để gửi lại cho các Feign client
 * (ví dụ Speech Service) sau khi MultipartFile gốc đã được consume/đọc bytes.
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content != null ? content : new byte[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (InputStream in = new ByteArrayInputStream(content)) {
            Files.copy(in, dest.toPath());
        }
    }

    @Override
    public void transferTo(java.nio.file.Path dest) throws IOException {
        try (InputStream in = new ByteArrayInputStream(content)) {
            Files.copy(in, dest);
        }
    }
}

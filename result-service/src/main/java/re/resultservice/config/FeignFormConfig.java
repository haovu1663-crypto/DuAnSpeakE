package re.resultservice.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình encoder cho Feign để hỗ trợ gửi multipart/form-data (MultipartFile)
 * tới các service khác (ví dụ Speech Service). Nếu không có config này, Feign sẽ
 * không encode được MultipartFile khi gọi qua SpeechClient.
 */
@Configuration
public class FeignFormConfig {

    @Bean
    public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
}

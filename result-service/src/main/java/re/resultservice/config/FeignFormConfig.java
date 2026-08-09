package re.resultservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration.
 * Spring Cloud 2025 + Spring Boot 4 hỗ trợ multipart/form-data qua OpenFeign
 * mà không cần thêm feign-form encoder thủ công.
 */
@Configuration
public class FeignFormConfig {
}

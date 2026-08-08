package re.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Route bằng Java Code cho Gateway Service.
 * Đảm bảo 100% các đường dẫn /api/** được định tuyến chính xác tới các Microservices.
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("pronunciation-service", r -> r.path("/api/pronunciation/**")
                        .filters(f -> f.stripPrefix(1)
                                .deduplicateResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("http://localhost:8082"))
                .route("speech-service", r -> r.path("/api/speech/**")
                        .filters(f -> f.stripPrefix(1)
                                .deduplicateResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("http://localhost:8081"))
                .route("ai-analysis-service", r -> r.path("/api/ai-analysis/**")
                        .filters(f -> f.stripPrefix(1)
                                .deduplicateResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("http://localhost:8083"))
                .route("result-service", r -> r.path("/api/result/**")
                        .filters(f -> f.stripPrefix(1)
                                .deduplicateResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("http://localhost:8084"))
                .build();
    }
}

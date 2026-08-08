package re.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Global filter để log tất cả các request đến Gateway.
 * Ghi lại method, URI, thời gian xử lý.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        log.info("[GATEWAY] --> {} {} | Time: {}",
                request.getMethod(),
                request.getURI(),
                LocalDateTime.now());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GATEWAY] <-- {} {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    request.getURI(),
                    exchange.getResponse().getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Chạy trước tất cả các filter khác
    }
}

package re.gatewayservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GateFilter implements GlobalFilter, Ordered {

    private final Logger log = LoggerFactory.getLogger(GateFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // Log TRƯỚC khi xử lý
        log.info(">> [REQUEST] method={}, path={}, remoteAddress={}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    // Log SAU khi xử lý thành công
                    log.info("<< [RESPONSE] path={}, status={}, duration={}ms",
                            request.getURI().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration);
                })
                .doOnError(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    // Log khi có lỗi xảy ra
                    log.error("<< [ERROR] path={}, duration={}ms, error={}",
                            request.getURI().getPath(),
                            duration,
                            throwable.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

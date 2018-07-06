package app;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.exec.util.ParallelBatch;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.client.HttpClient;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ratpack.jackson.Jackson.json;

public final class RecommendationsHandler implements Handler {

    private static final List<String> IDS = Arrays.asList(
            "PROD-001",
            "PROD-003",
            "PROD-004",
            "PROD-002",
            "PROD-005"
    );

    private static final List<String> BLACKLISTED = Arrays.asList(
            "PROD-003",
            "PROD-004",
            "PROD-005"
    );

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final MetricRegistry metricRegistry;

    @Inject
    public RecommendationsHandler(HttpClient http, ObjectMapper mapper, MetricRegistry metricRegistry) {
        this.http = http;
        this.mapper = mapper;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(Context ctx) {
        final List<Promise<Product>> promises = IDS.stream()
                .filter(id -> !BLACKLISTED.contains(id))
                .map(this::prepareHttpRequest)
                .collect(Collectors.toList());

        ParallelBatch.of(promises)
                .publisher()
                .filter(Objects::nonNull)
                .map(product -> product.applyDiscount(BigDecimal.valueOf(20)))
                .toList()
                .then(products -> ctx.render(json(new Payload(products.size(), "-20%", products))));
    }

    private Promise<Product> prepareHttpRequest(String id) {
        return http.get(URI.create("http://localhost:5050/product/" + id), request -> {
            request.connectTimeout(Duration.ofMillis(250));
            request.readTimeout(Duration.ofMillis(250));
        }).onError(error -> {
            Blocking.exec(() -> metricRegistry.counter("recommendation.timeout." + id).inc());
        }).mapIf(
                response -> response.getStatus().is2xx(),
                response -> mapper.readValue(response.getBody().getInputStream(), Product.class),
                response -> null
        );
    }

    static class Payload {
        public final int total;
        public final String discount;
        public final List<Product> products;

        Payload(int total, String discount, List<Product> products) {
            this.total = total;
            this.discount = discount;
            this.products = products;
        }
    }
}

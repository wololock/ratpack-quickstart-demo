package app

import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ConnectTimeoutException
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.exec.util.ParallelBatch
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.client.HttpClient
import ratpack.http.client.HttpClientReadTimeoutException

import javax.inject.Inject
import java.time.Duration

import static ratpack.jackson.Jackson.json

final class RecommendationsHandler implements Handler {

    private static final List<String> IDS = [
            'PROD-003',
            'PROD-002',
            'PROD-001',
            'PROD-004',
            'PROD-005',
    ]

    private static final List<String> BLACKLISTED = [
            'PROD-003',
            'PROD-004',
            'PROD-005',
    ]

    private final HttpClient http
    private final ObjectMapper mapper
    private final MetricRegistry metricRegistry

    @Inject
    RecommendationsHandler(HttpClient http, ObjectMapper mapper, MetricRegistry metricRegistry) {
        this.http = http
        this.mapper = mapper
        this.metricRegistry = metricRegistry
    }

    @Override
    void handle(Context ctx) throws Exception {
        final List<Promise<Product>> promises = (IDS - BLACKLISTED).collect { id ->
            http.get(URI.create("http://localhost:5050/product/${id}"), { request ->
                request.readTimeout(Duration.ofMillis(250))
                request.connectTimeout(Duration.ofMillis(250))
            }).onError { t ->
                switch (t.class) {
                    case ConnectTimeoutException:
                    case HttpClientReadTimeoutException:
                        Blocking.exec {
                            String productId = t.message.tokenize('/').last()
                            metricRegistry.counter("recommendations.timeout.${productId}").inc()
                        }
                        break
                }

            }.mapIf({ it.status.is2xx() }, { response ->
                mapper.readValue(response.body.inputStream, Product)
            })
        }

        ParallelBatch.of(promises)
                .publisher()
                .filter { product -> product != null }
                .map { product -> new Product(product.id, product.name, product.price * 0.8)}
                .toList()
                .then { products ->
                    ctx.render(json([
                            discount: '-20%',
                            total: products.size(),
                            products: products
                    ]))
                }
    }
}

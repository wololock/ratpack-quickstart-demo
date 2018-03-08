package app

import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.exec.Promise
import ratpack.exec.util.ParallelBatch
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse

import javax.inject.Inject
import java.nio.charset.Charset
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

    private final HttpClient http
    private final ObjectMapper mapper

    @Inject
    RecommendationsHandler(HttpClient http, ObjectMapper mapper) {
        this.http = http
        this.mapper = mapper
    }

    @Override
    void handle(Context ctx) throws Exception {
        println "[${System.currentTimeMillis()}] recommendations..."

        final List<Promise<ReceivedResponse>> promises = IDS.collect { id ->
            http.get(URI.create("http://localhost:5050/product/${id}"), { request ->
                request.readTimeout(Duration.ofMillis(300))
                request.connectTimeout(Duration.ofMillis(300))
            }).onError { t ->
                println "[${System.currentTimeMillis()}] onError executed: ${t.message}"
            }.mapIf({ it.status.is2xx() }, { response ->
                mapper.readValue(response.body.inputStream, Product)
            })
        }

        ParallelBatch.of(promises)
                .publisher()
                .toList()
                .then { products ->
                    ctx.render(json(products))
                }
    }
}

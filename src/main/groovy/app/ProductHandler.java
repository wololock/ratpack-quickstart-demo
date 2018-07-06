package app;

import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import java.util.Collections;

import static ratpack.jackson.Jackson.json;

public final class ProductHandler implements Handler {

    private final ProductService productService;

    @Inject
    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void handle(Context ctx) {
        productService.findProductById(ctx.getPathTokens().get("id")).onNull(() -> {
            ctx.getResponse().status(404);
            ctx.render(json(Collections.singletonMap("message", "Not Found")));
        }).then(product -> ctx.render(json(product)));
    }
}

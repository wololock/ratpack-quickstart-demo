package app

import ratpack.handling.Context
import ratpack.handling.Handler

import javax.inject.Inject

import static ratpack.jackson.Jackson.json

final class ProductHandler implements Handler {

    private final ProductService productService

    @Inject
    ProductHandler(ProductService productService) {
        this.productService = productService
    }

    @Override
    void handle(Context ctx) throws Exception {
        productService.findProductById(ctx.pathTokens.id).onNull {
            ctx.response.status(404)
            ctx.render(json([message: "Product not found"]))
        }.then { product ->
            ctx.render(json(product))
        }
    }
}

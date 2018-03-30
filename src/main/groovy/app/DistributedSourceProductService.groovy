package app

import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.func.Pair

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

final class DistributedSourceProductService implements ProductService {

    private final Map<String, Supplier<Product>> products = [
            Pair.of(100L, new Product('PROD-001', 'Learning Ratpack', 29.99)),
            Pair.of(130L, new Product('PROD-002', 'Implementing Domain-Driven Design', 39.99)),
            Pair.of(1200L, new Product('PROD-003', 'Groovy In Action', 34.99)),
            Pair.of(600L, new Product('PROD-004', 'Clean Code', 29.99)),
            Pair.of(1500L, new Product('PROD-005', 'Code Complete', 48.99))
    ].collectEntries { pair ->
        [(pair.right().id): {
            Thread.sleep(pair.left())
            return pair.right()
        } as Supplier<Product>]
    } as ConcurrentHashMap

    @Override
    Promise<Product> findProductById(String id) {
        return Blocking.get {
            products.getOrDefault(id, {} as Supplier).get()
        }
    }
}

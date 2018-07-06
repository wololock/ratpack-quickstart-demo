package app

import ratpack.exec.Blocking
import ratpack.exec.Promise

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

final class InMemoryFakeProductService implements ProductService {

    private final Map<String, Supplier<Product>> products = [
            'PROD-001': {
                Thread.sleep(100L)
                return new Product(id: 'PROD-001', name: 'Learning Ratpack', price: 29.99)
            } as Supplier<Product>,

            'PROD-002': {
                Thread.sleep(130L)
                return new Product(id: 'PROD-002', name: 'Netty In Action', price: 39.99)
            } as Supplier<Product>,

            'PROD-003': {
                Thread.sleep(600L)
                return new Product(id: 'PROD-003', name: 'Clean Code', price: 42.99)
            } as Supplier<Product>,

            'PROD-004': {
                Thread.sleep(1200L)
                return new Product(id: 'PROD-004', name: 'Groovy In Action', price: 34.99)
            } as Supplier<Product>,

            'PROD-005': {
                Thread.sleep(1500L)
                return new Product(id: 'PROD-005', name: 'Code Complete', price: 49.99)
            } as Supplier<Product>,
    ] as ConcurrentHashMap

    @Override
    Promise<Product> findProductById(String id) {
        return Blocking.get {
            products.getOrDefault(id, {} as Supplier).get()
        }
    }
}

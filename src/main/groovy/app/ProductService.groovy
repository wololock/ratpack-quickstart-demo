package app

import ratpack.exec.Promise

interface ProductService {
    Promise<Product> findProductById(String id)
}
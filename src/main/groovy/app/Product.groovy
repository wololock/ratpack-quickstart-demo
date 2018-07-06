package app

import groovy.transform.Immutable

@Immutable
class Product {
    String id
    String name
    BigDecimal price

    Product applyDiscount(BigDecimal discount) {
        return new Product(id, name, price * ((100 - discount) / 100))
    }
}

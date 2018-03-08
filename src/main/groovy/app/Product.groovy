package app

import groovy.transform.Immutable

@Immutable
class Product {
    String id
    String name
    BigDecimal price
}

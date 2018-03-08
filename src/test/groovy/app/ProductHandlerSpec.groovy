package app

import groovy.json.JsonSlurper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.http.Status
import ratpack.test.ApplicationUnderTest
import spock.lang.AutoCleanup
import spock.lang.Specification

class ProductHandlerSpec extends Specification {

    @AutoCleanup
    final ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()


    def "should return product PROD-002 as JSON response"() {
        when:
        def response = aut.httpClient.get('product/PROD-002')

        then:
        response.status == Status.OK

        and:
        new JsonSlurper().parseText(response.body.text).name == 'Implementing Domain-Driven Design'
    }

    def "should return 404 HTTP status if requested product does not exist"() {
        when:
        def response = aut.httpClient.get('product/PROD-666')

        then:
        response.statusCode == 404
    }
}

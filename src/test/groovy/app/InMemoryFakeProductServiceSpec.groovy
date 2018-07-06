package app

import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class InMemoryFakeProductServiceSpec extends Specification {

    @Subject
    @Shared
    InMemoryFakeProductService service = new InMemoryFakeProductService()

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness()

    def "should return Learning Ratpack product"() {
        when:
        def result = harness.yield {
            service.findProductById('PROD-001')
        }

        then:
        result.value.name == 'Learning Ratpack'

        and:
        result.value.id == 'PROD-001'
    }

    def "should return null for non-existing products"() {
        when:
        def result = harness.yield {
            service.findProductById('PROD-666')
        }

        then:
        !result.complete

        and:
        result.value == null
    }
}

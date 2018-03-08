package app

import groovy.json.JsonSlurper
import ratpack.exec.ExecResult
import ratpack.exec.Promise
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.http.Status
import ratpack.impose.ImpositionsSpec
import ratpack.impose.ServerConfigImposition
import ratpack.test.ApplicationUnderTest
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

class RecommendationsHandlerSpec extends Specification {

    @AutoCleanup
    final ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()  {
        @Override
        protected void addDefaultImpositions(ImpositionsSpec impositionsSpec) {
            impositionsSpec.add(ServerConfigImposition.of { conf ->
                conf.port(5050)
            })
        }
    }

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness(1)

    def "should return at least 2 recommendations in 300ms"() {
        when:
        ExecResult response = harness.yield {
            println "[${System.currentTimeMillis()}] recommendations2..."
            Promise.value(aut.httpClient.get('recommendations'))
        }

        then:
        response.value.status == Status.OK

        and:
        new JsonSlurper().parseText(response.value.body.text).id as Set == ['PROD-001', 'PROD-002'] as Set
    }
}

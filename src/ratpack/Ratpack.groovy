import app.InMemoryFakeProductService
import app.ProductHandler
import app.ProductService
import app.RecommendationsHandler
import ratpack.dropwizard.metrics.DropwizardMetricsModule

import static ratpack.groovy.Groovy.ratpack

ratpack {
    serverConfig {
        threads(1)
        development(false)
    }

    bindings {
        module (DropwizardMetricsModule) { conf ->
            conf.jmx()
        }
        bind ProductHandler
        bind RecommendationsHandler
        bindInstance ProductService, new InMemoryFakeProductService()
    }

    handlers {
        get("product/:id", ProductHandler)

        get("recommendations", RecommendationsHandler)
    }
}
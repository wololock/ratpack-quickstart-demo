import app.DistributedSourceProductService
import app.ProductHandler
import app.ProductService
import app.RecommendationsHandler
import ratpack.dropwizard.metrics.DropwizardMetricsModule

import static ratpack.groovy.Groovy.ratpack

ratpack {
    serverConfig {
        threads(4)
    }

    bindings {
        module (DropwizardMetricsModule) { conf ->
            conf.jmx()
        }
        bind ProductHandler
        bind RecommendationsHandler
        bindInstance ProductService, new DistributedSourceProductService()
    }

    handlers {
        get("product/:id", ProductHandler)

        get("recommendations", RecommendationsHandler)
    }
}
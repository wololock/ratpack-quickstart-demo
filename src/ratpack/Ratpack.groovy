import app.DistributedSourceProductService
import app.ProductHandler
import app.ProductService
import app.RecommendationsHandler

import static ratpack.groovy.Groovy.ratpack

ratpack {
    serverConfig {
        threads(4)
    }

    bindings {
        bind ProductHandler
        bind RecommendationsHandler
        bindInstance ProductService, new DistributedSourceProductService()
    }

    handlers {
        get("product/:id", ProductHandler)

        get("recommendations", RecommendationsHandler)
    }
}
package com.jskrzypczynski.poa.http

import cats.effect.IO
import com.jskrzypczynski.poa.config.HttpServerConfig
import com.jskrzypczynski.poa.http.routes.AggregationRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

class HttpServer(aggregationRoutes: AggregationRoutes, config: HttpServerConfig) {

  val api = Router(
    "api/aggregation/find" -> aggregationRoutes.getAggregation,
    "api/aggregation/close" -> aggregationRoutes.closeAggregation,
    "api/offer/supply" -> aggregationRoutes.supplyOffer,
    "api/aggregation/highestOffer" -> aggregationRoutes.getProductCodeWithHighestOffersCount
  ).orNotFound

  def start(): IO[Unit] =
    BlazeServerBuilder[IO](cats.effect.unsafe.IORuntime.global.compute)
      .bindHttp(config.port, "localhost")
      .withHttpApp(api)
      .serve
      .compile
      .drain

}

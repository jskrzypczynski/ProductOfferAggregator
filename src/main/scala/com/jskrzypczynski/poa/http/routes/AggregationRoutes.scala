package com.jskrzypczynski.poa.http.routes

import cats.effect.IO
import com.jskrzypczynski.poa.config.Config
import com.jskrzypczynski.poa.domain.{Aggregation, Offer, ProductCodeWithHighestOffersCount}
import com.jskrzypczynski.poa.http.OffersService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class AggregationRoutes(offersService: OffersService, config: Config) {

  val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val dsl: Http4sDsl[IO] = Http4sDsl[IO]

  import dsl._

  implicit val aggregationDecoder: EntityDecoder[IO, Aggregation] = jsonOf[IO, Aggregation]
  implicit val offerDecoder: EntityDecoder[IO, Vector[Offer]] = jsonOf[IO, Vector[Offer]]
  implicit val highestProductCodeDecoder: EntityDecoder[IO, ProductCodeWithHighestOffersCount] = jsonOf[IO, ProductCodeWithHighestOffersCount]
  implicit val highestProductCodeVecResDecoder: EntityDecoder[IO, Vector[ProductCodeWithHighestOffersCount]] = jsonOf[IO, Vector[ProductCodeWithHighestOffersCount]]

  def getAggregation: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / productCode =>
        logger.info(s"Receive request to find aggregation for: [$productCode]") >>
          offersService.getAggregation(productCode).flatMap {
            case Some(agg) => Ok(agg.asJson)
            case None => NotFound(s"Not found aggreagation for $productCode")
          }
    }
  }

  def closeAggregation: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / productCode =>
        logger.info(s"Receive request to close aggregation for: [$productCode]") >>
          offersService.closeAggregation(productCode).flatMap {
            case OffersService.ClosedAggregation(aggregation) => Ok(s"Successfully close aggregation for product: [${aggregation.productCode}]")
            case OffersService.AggregationNotFound => NotFound(s"Not found aggregation for $productCode")
            case OffersService.AggregationCantBeClosed(offersCount) => Ok(s"Cant close aggregation for product: [${productCode}], not enough offers: [$offersCount], limit: [${config.closeLimit}]")
          }
    }
  }

  def supplyOffer: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@POST -> Root =>
        for {
          _ <- logger.info(s"Receive batch of offers.")
          offers <- req.as[Vector[Offer]]
          aggregationResult <- offersService.aggregateOffers(offers)
          requestResult <- aggregationResult match {
            case Left(error) => InternalServerError(s"Couldn't supply aggregation with new offers: [${error.getMessage}]")
            case Right(_) => Ok(s"Successfully aggregate offers: [${offers.size}]")
          }
        } yield requestResult
    }
  }

  def getProductCodeWithHighestOffersCount: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root =>
        logger.info(s"Receive request to find product code with highest offers count") >>
          offersService.getProductCodeWithHighestOffersCount().flatMap(_.sortBy(_.maxNumOfOffers).reverse.headOption match {
            case Some(value) => Ok(value.asJson)
            case None => Ok(s"No aggregations")
          })
    }
  }

}



package com.jskrzypczynski.poa.aggregation

import cats.effect.IO
import com.jskrzypczynski.poa.db.AggregationDb
import com.jskrzypczynski.poa.domain.{Aggregation, Offer}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class OffersAggregator(aggregationDb: AggregationDb) {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def aggregateOffer(offer: Offer): IO[Unit] = {
    for {
      aggregation <- aggregationDb.findAggregation(offer.productCode)
      _ <- aggregation match {
        case Some(agg) if agg.status.trim.toLowerCase == "open" => enhanceExistingAggregation(offer, agg)
        case Some(_) => logger.warn(s"Aggregation for offer: [${offer.productCode}] is already closed")
        case None => createNewAggregation(offer)
      }
    } yield ()
  }

  private def enhanceExistingAggregation(offer: Offer, aggregation: Aggregation): IO[Unit] = {
    val newOffersVector: Vector[Offer] = aggregation.offers :+ offer
    val offersCount: Int = newOffersVector.size
    val minPrice: Float = if (offer.price < aggregation.minPrice) offer.price else aggregation.minPrice
    val maxPrice: Float = if (offer.price > aggregation.maxPrice) offer.price else aggregation.maxPrice
    val avgPrice: Float = newOffersVector.map(_.price).sum / offersCount
    val newAggregation = Aggregation(offer.productCode, minPrice, maxPrice, avgPrice, newOffersVector)

    aggregationDb.updateAggregation(newAggregation)
  }

  private def createNewAggregation(offer: Offer): IO[Unit] = for {
    _ <- IO.unit
    aggregation = Aggregation(offer.productCode, offer.price, offer.price, offer.price, 1, "open", Vector(offer))
    _ <- aggregationDb.insertAggregation(aggregation)
  } yield ()

}

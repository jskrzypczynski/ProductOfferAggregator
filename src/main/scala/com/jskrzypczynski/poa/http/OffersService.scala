package com.jskrzypczynski.poa.http

import cats.effect.IO
import cats.implicits._
import com.jskrzypczynski.poa.config.Config
import com.jskrzypczynski.poa.db.AggregationDb
import com.jskrzypczynski.poa.domain.{Aggregation, Offer, ProductCodeWithHighestOffersCount}
import com.jskrzypczynski.poa.http.OffersService.{AggregationCantBeClosed, AggregationNotFound, CloseAggregationResult, ClosedAggregation}
import com.jskrzypczynski.poa.queue.OffersQueue

class OffersService(aggregationDb: AggregationDb, offersQueue: OffersQueue, config: Config) {
  
  def aggregateOffers(offers: Vector[Offer]): IO[Either[Throwable, Unit]] = offers.parTraverse(offersQueue.enqueueOffer).void.attempt

  def getAggregation(productCode: String): IO[Option[Aggregation]] = aggregationDb.findAggregation(productCode)

  def closeAggregation(productCode: String): IO[CloseAggregationResult] = {
    for {
      aggregation <- aggregationDb.findAggregation(productCode)
      result = aggregation match {
        case Some(agg) => if (agg.offers.size > config.closeLimit) ClosedAggregation(agg) else AggregationCantBeClosed(agg.offers.size)
        case None => AggregationNotFound
      }
    } yield result
  }

  def getProductCodeWithHighestOffersCount(): IO[Vector[ProductCodeWithHighestOffersCount]] = aggregationDb.getProductCodeWithHighestOffersCount()

}

object OffersService {

  sealed trait CloseAggregationResult

  case class ClosedAggregation(aggregation: Aggregation) extends CloseAggregationResult

  object AggregationNotFound extends CloseAggregationResult

  case class AggregationCantBeClosed(offersCount: Int) extends CloseAggregationResult


}
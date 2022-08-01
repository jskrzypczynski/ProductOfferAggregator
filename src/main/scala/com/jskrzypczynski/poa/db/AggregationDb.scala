package com.jskrzypczynski.poa.db

import cats.effect._
import cats.implicits.toTraverseOps
import com.jskrzypczynski.poa.config.DatabaseConfig
import com.jskrzypczynski.poa.domain.{Aggregation, Offer, ProductCodeWithHighestOffersCount}
import doobie._
import doobie.h2._
import doobie.implicits._
import doobie.util.ExecutionContexts

import java.util.UUID

case class AggregationResult(productCode: String, minPrice: Int, maxPrice: Int, average: Float, numOffers: Int, status: String, offers: String) {
  def transferToAggregation(): Option[Aggregation] = {
    offers.split(";;").toVector.traverse(parseOffer).map(offers =>
      Aggregation(productCode, minPrice, maxPrice, average, numOffers, status, offers))
  }

  def parseOffer(offerRecord: String): Option[Offer] = {
    val offerVector = offerRecord.split(",").toVector
    offerVector match {
      case Vector(price, productCode) => Some(Offer(price.toFloat, productCode))
      case _ => None
    }

  }
}


class AggregationDb(transactor: H2Transactor[IO]) {

  def findAggregation(productCode: String): IO[Option[Aggregation]] = {
    findAggregationQuery(productCode).transact(transactor).map(_.flatMap(_.transferToAggregation()))
  }

  def insertAggregation(aggregation: Aggregation): IO[Unit] = {
    insertAggregationQuery(aggregation).transact(transactor).void
  }

  def updateAggregation(aggregation: Aggregation): IO[Unit] = {
    updateAggregationQuery(aggregation).transact(transactor).void
  }

  def closeAggregation(aggregation: Aggregation): IO[Unit] = {
    closeAggregationQuery(aggregation.productCode).transact(transactor).void
  }

  def getProductCodeWithHighestOffersCount(): IO[Vector[ProductCodeWithHighestOffersCount]] = {
    getAggregationWithHighestOffersCountQuery().transact(transactor)
  }

  private def findAggregationQuery(productCode: String): ConnectionIO[Option[AggregationResult]] = {
    (sql"Select * FROM aggregations where" ++ fr" productCode = $productCode").query[AggregationResult].option
  }

  private def insertAggregationQuery(aggregation: Aggregation): ConnectionIO[Int] = {
    val offers: String = aggregation.offers.map(_.toString).mkString(";;")
    sql"""insert into aggregations (productCode, minPrice, maxPrice, avg, numOffers, status, offers) 
         values (${aggregation.productCode}, ${aggregation.minPrice}, ${aggregation.maxPrice}, ${aggregation.average}, ${aggregation.numOffers}, ${aggregation.status}, $offers )
         """.update.run
  }

  private def updateAggregationQuery(aggregation: Aggregation): ConnectionIO[Int] = {
    val offers: String = aggregation.offers.map(_.toString).mkString(";;")
    sql"""update aggregations set minPrice =  ${aggregation.minPrice}, maxPrice = ${aggregation.maxPrice}, avg = ${aggregation.average},  
                        numOffers = ${aggregation.numOffers},  offers =  $offers where productCode = ${aggregation.productCode}
         """.update.run
  }

  private def closeAggregationQuery(productCode: String): ConnectionIO[Int] = {
    sql"update aggregations set status = 'closed' where productCode = $productCode".update.run
  }

  private def getAggregationWithHighestOffersCountQuery(): ConnectionIO[Vector[ProductCodeWithHighestOffersCount]] = {
    (sql"Select productCode, MAX(numOffers) as maxNumOfOffers FROM aggregations group by productCode").query[ProductCodeWithHighestOffersCount].to[Vector]
  }

}


object Database {

  private val createTableSql =
    sql"""Create TABLE aggregations (productCode VARCHAR(200), minPrice Integer, maxPrice Integer, avg DECIMAL(15,4), numOffers INTEGER, status VARCHAR(20), offers CHARACTER LARGE OBJECT)
         |""".stripMargin

  def apply(config: DatabaseConfig): Resource[IO, AggregationDb] = {

    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](config.connectionPoolSize)
      transactor <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "", ce)
      _ <- Resource.eval(createTableSql.update.run.transact(transactor))
      aggregationDb = new AggregationDb(transactor)
    } yield aggregationDb

  }
}

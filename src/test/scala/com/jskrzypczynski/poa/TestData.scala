package com.jskrzypczynski.poa

import cats.effect.unsafe.implicits.global
import com.jskrzypczynski.poa.config.DatabaseConfig
import com.jskrzypczynski.poa.db.{AggregationResult, Database}
import com.jskrzypczynski.poa.domain.{Aggregation, Offer}


object TestData {

  val dbConfig: DatabaseConfig = DatabaseConfig(10)

  val (aggDb, dbCloseAction) = ((for {
    db <- Database.apply(dbConfig)
  } yield db).allocated).unsafeRunSync()

  val aggregation1: Aggregation = Aggregation("p1", 10, 20, 15f, 2, "Open", Vector(Offer(10, "p1"), Offer(20, "p1")))
  val aggregationResult1: AggregationResult = AggregationResult("t1", 10f, 15f, 12f, 2, "open", "10.0,t1;;15.23,t1")
  val transferredAggregationResult1: Aggregation = Aggregation("t1", 10f, 15f, 12f, Vector(Offer(10f, "t1"), Offer(15.23f, "t1")))

}

package com.jskrzypczynski.poa.db

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.jskrzypczynski.poa.TestData.{aggDb, aggregation1, dbCloseAction}
import com.jskrzypczynski.poa.domain.Aggregation
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class AggregationDbSpec extends AsyncWordSpec
  with Matchers
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  override def afterEach(): Unit = {
    aggDb.truncateTable().unsafeRunSync()
  }

  override def afterAll(): Unit = {
    dbCloseAction.unsafeRunSync()
  }

  "AggregationDb" can {
    "persist operations" should {
      "save aggregation and read" in {

        val resultIO: IO[Option[Aggregation]] = for {
          _ <- aggDb.insertAggregation(aggregation1)
          res <- aggDb.findAggregation("p1")
        } yield res

        (for {
          result <- resultIO
          assert = result shouldBe defined
          _ = result shouldBe Some(aggregation1)
        } yield assert).unsafeRunSync()
      }

      "close aggregation and read after change" in {
        val resultIO: IO[Option[Aggregation]] = for {
          _ <- aggDb.insertAggregation(aggregation1)
          _ <- aggDb.closeAggregation(aggregation1)
          res <- aggDb.findAggregation("p1")
        } yield res

        (for {
          result <- resultIO
          assert = result shouldBe defined
          _ = result shouldBe Some(aggregation1.copy(status = "closed"))
        } yield assert).unsafeRunSync()

      }

      "update aggregation and read after change" in {
        val resultIO: IO[Option[Aggregation]] = for {
          _ <- aggDb.insertAggregation(aggregation1)
          _ <- aggDb.updateAggregation(aggregation1.copy(minPrice = 1500.00f))
          res <- aggDb.findAggregation("p1")
        } yield res

        (for {
          result <- resultIO
          assert = result shouldBe defined
          _ = result shouldBe Some(aggregation1.copy(minPrice = 1500.00f))
        } yield assert).unsafeRunSync()

      }
    }
  }
}

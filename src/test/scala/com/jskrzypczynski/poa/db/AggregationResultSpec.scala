package com.jskrzypczynski.poa.db

import com.jskrzypczynski.poa.TestData.{aggregationResult1, transferredAggregationResult1}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class AggregationResultSpec extends AsyncWordSpec with Matchers {

  "AggregationResult" can {
    "transfer aggregations" should {
      "properly transfer aggregation" in {

        val transferResult = aggregationResult1.transferToAggregation()
        transferResult shouldBe Some(transferredAggregationResult1)
      }
    }
  }

}

package com.jskrzypczynski.poa.domain

case class Aggregation(productCode: String, minPrice: Float, maxPrice: Float, average: Float, numOffers: Int, status: String, offers: Vector[Offer])

object Aggregation {

  def apply(productCode: String, minPrice: Float, maxPrice: Float, average: Float, offers: Vector[Offer]): Aggregation =
    new Aggregation(productCode, minPrice, maxPrice, average, offers.size, "open", offers)
  
}
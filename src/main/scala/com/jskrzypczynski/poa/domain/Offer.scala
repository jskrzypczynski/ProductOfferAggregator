package com.jskrzypczynski.poa.domain

case class Offer(price: Float, productCode: String) {
  override def toString: String = s"$price,$productCode"
}
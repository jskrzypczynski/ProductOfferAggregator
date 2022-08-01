package com.jskrzypczynski.poa.queue

import cats.effect.std.Queue
import cats.effect.{IO, Resource}
import com.jskrzypczynski.poa.aggregation.OffersAggregator
import com.jskrzypczynski.poa.config.QueueConfig
import com.jskrzypczynski.poa.domain.Offer
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class OffersQueue private(queue: Queue[IO, Option[Offer]],
                          offersAggregator: OffersAggregator) {

  val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def enqueueOffer(offer: Offer): IO[Unit] = {
    queue.tryOffer(Some(offer)).flatMap {
      case true => logger.debug(s"Successfully enqueued offer")
      case false => logger.warn(s"Dropping Offer due to full queue.")
    }
  }

  private def runQueue(): IO[Unit] = Stream.fromQueueNoneTerminated(queue)
    .evalMap(offer => offersAggregator.aggregateOffer(offer))
    .compile
    .drain
    .guarantee(logger.info(s"Processing offers by OffersQueue terminated"))

  private def initiateShutdown(): IO[Unit] =
    logger.info("Starting graceful shutdown off OffersAggregatorQueue.") >>
      queue.offer(None)

}

object OffersQueue {

  def apply(offersAggregator: OffersAggregator, config: QueueConfig): Resource[IO, OffersQueue] = for {
    queue <- Resource.eval(Queue.bounded[IO, Option[Offer]](config.queueCapacity))
    offersQueue = new OffersQueue(queue, offersAggregator)
    _ <- Resource.make(offersQueue.runQueue().start)(fiber => offersQueue.initiateShutdown() *> fiber.join.void)
  } yield offersQueue

}
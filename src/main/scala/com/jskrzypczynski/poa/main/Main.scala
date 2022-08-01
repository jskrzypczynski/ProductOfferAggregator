package com.jskrzypczynski.poa.main

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxMonadError
import com.jskrzypczynski.poa.aggregation.OffersAggregator
import com.jskrzypczynski.poa.config.Config
import com.jskrzypczynski.poa.db.Database
import com.jskrzypczynski.poa.file.FileReader
import com.jskrzypczynski.poa.http.routes.AggregationRoutes
import com.jskrzypczynski.poa.http.{HttpServer, OffersService}
import com.jskrzypczynski.poa.queue.OffersQueue
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

import java.nio.file.Path

object Main extends IOApp {

  val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- logger.info("Starting the app...")
    configPath <- getConfigPath(args)
    config <- loadConfig(configPath)
    fileReader <- FileReader(config.fileReader)
    (aggregationDb, closeDatabaseConnections) <- Database(config.database).allocated
    offersAggregator = new OffersAggregator(aggregationDb)
    (offersQueue, closeQueue) <- OffersQueue(offersAggregator, config.queue).allocated
    offersService = new OffersService(aggregationDb, offersQueue, config)
    aggregationRoutes = new AggregationRoutes(offersService, config)
    _ <- if (config.fileReader.startupAggregation) loadStartupAggregation(fileReader, offersService) else IO.unit
    httpServer = new HttpServer(aggregationRoutes, config.httpServer)
    _ <- httpServer.start()
    _ <- logger.info("App started.")
    _ <- closeDatabaseConnections
    _ <- closeQueue
  } yield ExitCode.Success

  private def loadStartupAggregation(fileReader: FileReader, offersService: OffersService): IO[Unit] = {
    for {
      offersE <- fileReader.loadOffersFromFile()
      _ <- offersE match {
        case Left(error) => logger.error(s"Cant load offers from file, reason: [${error.getMessage}]")
        case Right(offers) => offersService.aggregateOffers(offers) >> logger.info(s"Successfully initialize application with startup aggregations")
      }
    } yield ()
  }

  private def loadConfig(customConfigLocation: Path): IO[Config] =
    logger.info(s"Loading configuration from [$customConfigLocation].") >>
      ConfigSource.default(ConfigSource.file(customConfigLocation)).loadF[IO, Config]()
        .attemptTap {
          case Right(config) => logger.info(s"Starting with config [$config].")
          case Left(err: Throwable) => logger.error(err)(s"Error occurred while reading config.")
        }

  private def getConfigPath(value: List[String]): IO[Path] = {
    value match {
      case path :: Nil => IO(Path.of(path))
      case _ :: _ :: Nil => IO.raiseError(new Throwable("Please provide only config path parameter"))
      case _ => IO.raiseError(new Throwable("Please provide config path"))
    }
  }

}

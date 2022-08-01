package com.jskrzypczynski.poa.file

import cats.effect.IO
import cats.implicits.toFunctorFilterOps
import com.jskrzypczynski.poa.config.FileReaderConfig
import com.jskrzypczynski.poa.domain.Offer
import fs2.Stream
import fs2.io.file.{Files, Flags, Path}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.file.Paths


class FileReader(offersFilePath: Path, batchSize: Int, maxConcurrentReads: Int)(implicit logger: Logger[IO]) {

  def loadOffersFromFile(): IO[Either[Throwable, Vector[Offer]]] = {
    loadBatchOfOffersFromFile.parEvalMapUnordered(maxConcurrentReads) { offer =>
      offer.split(",", -1).toVector match {
        case Vector(_, price, _, _, productCode, _, _, _, _, _, _, _, _, _, _, _, _) =>
          val parsedPrice = price.toFloatOption
          if (!productCode.isBlank) logger.debug(s"Successfully parsed record [$offer]") >> IO(parsedPrice.map(Offer(_, productCode.trim.toLowerCase)))
          else logger.warn(s"Empty product code field for offer: [$offer]") >> IO(None)
        case _ =>
          logger.warn(s"Cant parse offer record: [$offer]") >> IO(None)
      }
    }.filter(_.nonEmpty).flattenOption.compile.toVector.attempt
  }

  private def loadBatchOfOffersFromFile: Stream[IO, String] = {
    Files[IO].readAll(offersFilePath, batchSize, Flags.Read)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .filter(_.nonEmpty)
  }

}

object FileReader {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def apply(config: FileReaderConfig): IO[FileReader] = {
    if (java.nio.file.Files.exists(Paths.get(config.path))) {
      IO(new FileReader(Path(config.path), config.batchSize, config.maxConcurrentReads))
    } else {
      IO.raiseError(new Throwable(s"Path doesnt exists: [$config.path]"))
    }
  }

}

  
  
  
  
  


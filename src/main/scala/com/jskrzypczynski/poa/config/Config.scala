package com.jskrzypczynski.poa.config

case class HttpServerConfig(port: Int)

case class DatabaseConfig(connectionPoolSize: Int)

case class FileReaderConfig(path: String, batchSize: Int, maxConcurrentReads: Int, startupAggregation: Boolean)

case class QueueConfig(queueCapacity: Int)

case class Config(httpServer: HttpServerConfig,
                  database: DatabaseConfig,
                  fileReader: FileReaderConfig,
                  queue: QueueConfig,
                  closeLimit: Int = 3)

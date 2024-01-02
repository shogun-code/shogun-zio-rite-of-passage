package io.shogun.reviewboard

import io.shogun.reviewboard.http.HttpApi
import io.shogun.reviewboard.services.CompanyService
import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}

object Application extends ZIOAppDefault {
  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _         <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(endpoints.toList)
    )
    _ <- Console.printLine("Rock the JVM Review Board is running...")
  } yield ()

  override def run = serverProgram
    .provide(
      Server.default,
      CompanyService.dummyLayer
  )
}


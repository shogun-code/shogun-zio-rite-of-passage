package io.shogun.reviewboard

import io.shogun.reviewboard.http.HttpApi
import io.shogun.reviewboard.repositories.{CompanyRepositoryLive, Repository}
import io.shogun.reviewboard.services.{CompanyService, CompanyServiceLive}
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
      //services//services
      CompanyServiceLive.layer,
      //repos
      CompanyRepositoryLive.layer,
      //other layers
      Repository.dataLayer // quill & db-connection
  )
}


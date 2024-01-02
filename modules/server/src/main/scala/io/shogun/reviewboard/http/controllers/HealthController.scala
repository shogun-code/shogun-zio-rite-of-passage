package io.shogun.reviewboard.http.controllers

import io.shogun.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

import collection.mutable


class HealthController extends BaseController with HealthEndpoint {
  val health = healthEndpoint.serverLogicSuccess[Task](_ => ZIO.succeed("OK OK"))

  override val routes: List[ServerEndpoint[Any, Task]] = List(health)
}

object HealthController {
  def makeZIO: ZIO[Any, Nothing, HealthController] = ZIO.succeed(new HealthController())
}

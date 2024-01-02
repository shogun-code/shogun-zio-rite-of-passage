package io.shogun.reviewboard.http

import io.shogun.reviewboard.http.controllers.{BaseController, CompanyController, HealthController}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}
import io.shogun.reviewboard.repositories.CompanyRepositoryDemo.validateEnv

object HttpApi {
  def gatherRoutes(controllers: List[BaseController]): Seq[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)
    
  def makeControllers: ZIO[Any, Nothing, List[BaseController]] = for {
    health <- HealthController.makeZIO
    companies <- CompanyController.makeZIO
  } yield List(health, companies)
  
  val endpointsZIO = makeControllers.map(gatherRoutes)
}

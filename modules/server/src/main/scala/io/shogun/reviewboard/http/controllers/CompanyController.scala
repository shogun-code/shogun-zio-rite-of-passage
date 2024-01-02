package io.shogun.reviewboard.http.controllers

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.services.CompanyService
import io.shogun.reviewboard.http.endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint
import zio.Task

import collection.mutable
import zio.*

class CompanyController private (service: CompanyService) extends BaseController
  with CompanyEndpoints {
  val db = mutable.Map[Long, Company]()

  // create
  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogicSuccess { req =>
    service.create(req)
  }

  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogicSuccess { _ =>
    service.getAll()
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogicSuccess { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById(_))
      .catchSome {
        case _: NumberFormatException => ZIO.fail(new Exception("Invalid id"))

      }
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}

object CompanyController {
  val makeZIO: ZIO[CompanyService, Nothing, CompanyController] =
    for {
      service <- ZIO.service[CompanyService]
    } yield new CompanyController(service)
}
package io.shogun.reviewboard.http.controllers

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import io.shogun.reviewboard.services.CompanyService
import zio.*
import zio.json.*
import sttp.tapir.generic.auto.*
import zio.test.*
import sttp.client3.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.RIOMonadError
import io.shogun.reviewboard.syntax.*

object CompanyControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val company = Company(1, "Rock the JVM", "rockthejvm.com", "rock-the-jvm")
  private val serviceStub = new CompanyService {
    override def create(name: String, website: String): Task[Company] =
      ZIO.succeed(company)
    override def getAll: Task[List[Company]] =
      ZIO.succeed(List(company))
    override def getById(id: Long): Task[Option[Company]] =
      if (id == 1) Some(company)
      else None
    override def getBySlug(slug: String): Task[Option[Company]] =
      if (slug == company.slug) Some(company)
      else None
  }

  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) =
    for {
    // create controller
    controller <- CompanyController.makeZIO
    backendStub <- ZIO.succeed(
      TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointRunLogic(
          endpointFun(controller)
        )
        .backend()
    )

  } yield backendStub

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyController")(
      test("post company") {
        val program = for {
          // create controller
          backendStub <- backendStubZIO(_.create)
          // create request
          rsp <- basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)
            .send(backendStub)
        } yield rsp.body
        // inspect response
        program.assert { rspBody =>
            rspBody.toOption.flatMap(_.fromJson[Company].toOption)
              .contains(Company(1, "Rock the JVM", "rockthejvm.com", "rock-the-jvm"))
          }
      },

      test("get all companies") {
        val program = for {
          // create controller
          backendStub <- backendStubZIO(_.getAll)
          // create request
          rsp <- basicRequest
            .get(uri"/companies")
            .send(backendStub)
        } yield rsp.body
        // inspect response
        assertZIO(program)(
          Assertion.assertion("inspect http response from getAll") {rspBody =>
            rspBody.toOption.flatMap(_.fromJson[List[Company]].toOption)
              .contains(List())
          }
        )
      },

      test("get company by id") {
        val program = for {
          // create controller
          backendStub <- backendStubZIO(_.getById)
          // create request
          rsp <- basicRequest
            .get(uri"/companies/1")
            .send(backendStub)
        } yield rsp.body
        // inspect response
        assertZIO(program)(
          Assertion.assertion("inspect http response from getById") {rspBody =>
            rspBody.toOption.flatMap(_.fromJson[Company].toOption)
              .isEmpty
          }
        )
      },
    )
      .provide(ZLayer.succeed(serviceStub))
}

package io.shogun.reviewboard.http.endpoints

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait CompanyEndpoints {
  val createEndpoint =
    endpoint
      .tag("companies")
      .name("create company")
      .description("create a new company")
      .post
      .in("companies")
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint =
    endpoint
      .tag("companies")
      .name("get all companies")
      .description("get all companies")
      .get
      .in("companies")
      .out(jsonBody[List[Company]])

  val getByIdEndpoint =
    endpoint
      .tag("companies")
      .name("get company by id")
      .description("get a company by its id")
      .get
      .in("companies" / path[String]("id"))
      .out(jsonBody[Option[Company]])
}

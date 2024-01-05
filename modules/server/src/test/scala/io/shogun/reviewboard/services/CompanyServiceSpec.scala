package io.shogun.reviewboard.services

import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import io.shogun.reviewboard.repositories.CompanyRepository
import io.shogun.reviewboard.domain.data.Company

import collection.mutable
import zio.*
import zio.test.*

object CompanyServiceSpec extends ZIOSpecDefault {
  private val service = ZIO.serviceWithZIO[CompanyService]

  private val stubRepoLayer = ZLayer.succeed(
    new CompanyRepository {
      val db: mutable.Map[Long, Company] = mutable.Map.empty

      override def create(company: Company): Task[Company] =
        ZIO.succeed:
          val newId = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = newId)
          db += (newId -> newCompany)
          newCompany

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt:
          val company = db(id)
          val updated = op(company)
          db += (id -> updated)
          updated

      override def delete(id: Long): Task[Company] =
        ZIO.attempt:
          val company = db(id)
          db -= id
          company

      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))

      override def getAll: Task[List[Company]] =
        ZIO.succeed(db.values.toList)
    }
  )
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyServiceTest")(
      test("create") {
        val companyZIO = service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
        companyZIO.assert { company =>
          company.name == "Rock the JVM" &&
            company.url == "rockthejvm.com" &&
            company.slug == "rock-the-jvm"
        }
      }
    ).provide(
      CompanyServiceLive.layer,
      stubRepoLayer
    )
}


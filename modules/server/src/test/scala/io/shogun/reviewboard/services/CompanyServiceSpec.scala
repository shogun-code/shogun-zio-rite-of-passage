package io.shogun.reviewboard.services

import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import io.shogun.reviewboard.repositories.CompanyRepository
import io.shogun.reviewboard.domain.data.Company

import collection.mutable
import zio.*
import zio.test.*
import io.shogun.reviewboard.syntax.assert

object CompanyServiceSpec extends ZIOSpecDefault {
  private val service = ZIO.serviceWithZIO[CompanyService]

  private val stubRepoLayer = ZLayer.succeed(
    new CompanyRepository {
      val db: mutable.Map[Long, Company] = mutable.Map[Long, Company]()

      override def create(company: Company): Task[Company] =
        ZIO.succeed {
          val newId = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = newId)
          db += (newId -> newCompany)
          newCompany
        }

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          val updated = op(company)
          db += (id -> updated)
          updated
        }

      override def delete(id: Long): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          db -= id
          company
        }

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
      },

      test("get by id") {
        // create a company
        // fetch a company by its id
        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          companyOpt <- service(_.getById(company.id))
        } yield (company, companyOpt)

        program.assert{
          case (company, Some(companyRes)) =>
            company.name == "Test test" &&
              company.url ==  "test.com" &&
              company.slug == "test-test" &&
              company == companyRes
          case _ => false
        }
      },

      test("get by slug") {
        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          companyOpt <- service(_.getBySlug(company.slug))
        } yield (company, companyOpt)

        program.assert{
          case (company, Some(companyRes)) =>
            company.name == "Test test" &&
              company.url ==  "test.com" &&
              company.slug == "test-test" &&
              company == companyRes
          case _ => false
        }
      },

      test("get all") {

        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          company2 <- service(_.create(CreateCompanyRequest("Google", "google.com")))
          companies <- service(_.getAll)
        } yield (company, company2, companies)

        program.assert {
          case (company, company2, companies) =>
            companies.toSet == Set(company, company2)
          case _ => false
        }
      }
    ).provide(
      CompanyServiceLive.layer,
      stubRepoLayer
    )
}


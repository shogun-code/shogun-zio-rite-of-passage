package io.shogun.reviewboard.repositories

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.repositories.RepositorySpec
import zio.test.*
import zio.*

import java.sql.SQLException
import javax.sql.DataSource
import io.shogun.reviewboard.syntax.assert
object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  private val testCompany = Company(-1L, "test-test", "Test test", "test.com")

  private def genString(): String =
    scala.util.Random.alphanumeric.take(8).mkString

  override val initScript: String = "sql/companies.sql"

  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString()
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(testCompany)
        } yield company

        program.assert {
          case Company(_, "test-test", "Test test", "test.com", _, _, _, _, _) => true
          case _ => false
        }
      },

      test("creating a duplicate should error") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          _ <- repo.create(testCompany)
          err <- repo.create(testCompany).flip
        } yield err

        program.assert(_.isInstanceOf[SQLException])

      },
      test("get by id and slug") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(testCompany)
          fetchById <- repo.getById(company.id)
          fetchSlug <- repo.getBySlug(company.slug)

        } yield (company, fetchById, fetchSlug)

        program.assert { case (company, fetchById, fetchSlug) =>
          fetchById.contains(company) && fetchSlug.contains(company)
        }

      },
      test("update records") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(testCompany)
          updated <- repo.update(company.id, _.copy(url = "newtest.com"))
          fetchById <- repo.getById(company.id)
        } yield (updated, fetchById)

        program.assert { case (updated, fetchById) =>
          fetchById.contains(updated)
        }
      },
      test("for delete") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(testCompany)
          _ <- repo.delete(company.id)
          fetchById <- repo.getById(company.id)
        } yield fetchById

        program.assert(_.isEmpty)
      },
      test("for get all records") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          companies <- ZIO.foreach(1 to 10)(_ => repo.create(genCompany()))
          companiesFetched <- repo.getAll
        } yield (companies, companiesFetched)

        program.assert { case (companies, companiesFetched) =>
          companies.toSet == companiesFetched.toSet
        }
      }
    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}

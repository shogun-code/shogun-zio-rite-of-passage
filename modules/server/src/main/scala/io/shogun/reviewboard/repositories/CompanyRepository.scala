package io.shogun.reviewboard.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import io.shogun.reviewboard.domain.data.Company

trait CompanyRepository {
  def create(company: Company): Task[Company]

  def update(id: Long, op: Company => Company): Task[Company]

  def delete(id: Long): Task[Company]

  def getAll: Task[List[Company]]

  def getById(id: Long): Task[Option[Company]]

  def getBySlug(slug: String): Task[Option[Company]]
}

class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {

  import quill.*

  inline given schema: SchemaMeta[Company] =
    schemaMeta[Company]("companies") // specify the table name

  inline given instMeta: InsertMeta[Company] =
    insertMeta[Company](_.id) // columns to be excluded from insert

  inline given upMeta: UpdateMeta[Company] =
    updateMeta[Company](_.id) // columns to be excluded from update

  override def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(c => c)
    }

  override def update(id: Long, op: Company => Company): Task[Company] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Company with id $id not found"))
      updated <- run {
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(current)))
          .returning(c => c)
      }
    } yield updated

  override def delete(id: Long): Task[Company] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .delete
        .returning(c => c)
    }

  override def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.slug == lift(slug))
    }.map(_.headOption)

  override def getAll: Task[List[Company]] =
    run {
      query[Company]
    }
}

object CompanyRepositoryLive {
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(quill => CompanyRepositoryLive(quill))
  }
}

object CompanyRepositoryDemo extends ZIOAppDefault {
  val program = for {
    repo <- ZIO.service[CompanyRepository]
    _ <- repo.create(Company(-1L, "rock-the-jvm", "Rock The JVM", "rockthejvm.com"))
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase), //quill instance
      Quill.DataSource.fromPrefix("shogun.db")
    )
}
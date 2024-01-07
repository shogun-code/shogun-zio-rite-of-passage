package io.shogun.reviewboard.services

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import io.shogun.reviewboard.repositories.CompanyRepository
import zio.*

import scala.collection.mutable

// Business logic
trait CompanyService {
  def create(req: CreateCompanyRequest) : Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
}



class CompanyServiceLive private(repo: CompanyRepository) extends CompanyService{
  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll: Task[List[Company]] =
    repo.getAll

  override def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repo.getBySlug(slug)
}

object CompanyServiceLive {
  val layer = ZLayer{
    for {
      repo <- ZIO.service[CompanyRepository]
    } yield new CompanyServiceLive(repo)
  }
}

// controller (http) -> service (business) -> repo (database)

//class CompanyServiceDummy extends CompanyService {
//  val db: mutable.Map[Long, Company] = mutable.Map[Long, Company]()
//
//  override def create(req: CreateCompanyRequest): Task[Company] =
//    ZIO.succeed {
//      val newId = db.keys.maxOption.getOrElse(0L) + 1
//      val newCompany = req.toCompany(newId)
//      db += (newId -> newCompany)
//      newCompany
//    }
//
//  override def getAll: Task[List[Company]] = ZIO.succeed(db.values.toList)
//
//  override def getById(id: Long): Task[Option[Company]] = ZIO
//    .attempt(id)
//    .map(db.get)
//
//  override def getBySlag(slug: String): Task[Option[Company]] = ZIO.succeed(
//    db.values.find(_.slug == slug)
//  )
//object CompanyService {
//  val dummyLayer = ZLayer.succeed(new CompanyServiceDummy)
//}
//}
package io.shogun.reviewboard.services

import io.shogun.reviewboard.domain.data.Company
import io.shogun.reviewboard.http.requests.CreateCompanyRequest
import zio.*

import scala.collection.mutable

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
}

object CompanyService {
  val dummyLayer = ZLayer.succeed(new CompanyServiceImpl)
}

class CompanyServiceImpl extends CompanyService {
  val db = mutable.Map[Long, Company]()

  override def create(req: CreateCompanyRequest): Task[Company] =
    ZIO.succeed {
      val id = db.keys.maxOption.getOrElse(0L) + 1
      val company = req.toCompany(id)
      db += (id -> company)
      company
    }

  override def getAll(): Task[List[Company]] = ZIO.succeed(db.values.toList)

  override def getById(id: Long): Task[Option[Company]] = 
    ZIO.succeed(db.get(id))

  override def getBySlug(slug: String): Task[Option[Company]] = 
    ZIO.succeed(db.values.find(_.slug == slug))
}


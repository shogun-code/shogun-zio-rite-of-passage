package io.shogun.reviewboard.repositories

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import zio.ZLayer
object Repository {
  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)

  val dataSourceLayer = Quill.DataSource.fromPrefix("shogun.db")

  val dataLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    dataSourceLayer >>> quillLayer
}

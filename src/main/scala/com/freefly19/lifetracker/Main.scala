package com.freefly19.lifetracker

import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global


object Main extends StreamApp[IO] {
  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    sys.env.getOrElse("JDBC_URL", "jdbc:postgresql://localhost:5433/life-tracker"),
    sys.env.getOrElse("JDBC_USER", "postgres"),
    sys.env.getOrElse("JDBC_PASSWORD", "")
  )

  val statement = sql"""create table if not exists locations(id bigserial primary key, longitude decimal, latitude decimal, created_at timestamp)"""

  statement.update.run.transact(xa).unsafeRunSync()


  implicit val decoder = jsonOf[IO, LocationInputDto]

  private val defaultService = HttpService[IO] {
    case GET -> Root / "api" / "locations" =>
      Ok(
        sql"""select * from locations"""
        .query[LocationOutputDto]
        .stream
        .compile
        .to[List]
        .transact(xa)
        .map(list => list.asJson)
      )

    case req @ POST -> Root / "api" / "locations" =>
      for {
        location <- req.as[LocationInputDto]
        id <- sql"insert into locations(longitude,latitude,created_at) values (${location.longitude}, ${location.latitude}, now()) RETURNING id".query[Long].unique.transact(xa)
        resp <- Ok(LocationOutputDto(id, location.longitude, location.latitude).asJson)
      } yield resp
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(9090, "0.0.0.0")
      .mountService(defaultService, "/")
      .serve
}


case class LocationInputDto(longitude: Double, latitude: Double)
case class LocationOutputDto(id: Long, longitude: Double, latitude: Double)
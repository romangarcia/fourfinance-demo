package model

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import play.api.db.DBApi

import scala.concurrent.duration.FiniteDuration

trait LoansRepository {
  def add(request: LoanRequest): StoredLoanRequest
  def updateApproved(request: StoredLoanRequest): StoredLoanRequest
  def getByID(id: UUID): StoredLoanRequest
  def countByClientIP(clientIP: String, forDate: DateTime): Int
}

class JdbcLoansRepository(dbApi: DBApi) extends LoansRepository {
  import anorm._
  import anorm.JodaParameterMetaData._
  import anorm.SqlParser._

  private lazy val db = dbApi.database("default")

  private val approvalParser = (
    str("id") ~
      str("first_name") ~
      str("last_name") ~
      get[BigDecimal]("amount") ~
      int("term_days").map(new FiniteDuration(_, TimeUnit.DAYS)) ~
      date("timestamp").map(t => new DateTime(t.getTime)) ~
      str("client_ip") ~
      bool("approved")
    ).map {
    case id ~ firstName ~ lastName ~ amount ~ termDays ~ timestamp ~ clientIP ~ approved =>
      val request = LoanRequest(firstName, lastName, amount, termDays, timestamp, clientIP)
      StoredLoanRequest(UUID.fromString(id), request, approved)
  }

  override def add(request: LoanRequest): StoredLoanRequest = {
    db.withConnection { implicit conn =>
      val id = UUID.randomUUID()
      SQL"""
        INSERT INTO loans (id, first_name, last_name, amount, term_days, timestamp, client_ip, approved)
        VALUES (${id.toString}, ${request.firstName}, ${request.lastName}, ${request.amount},
                ${request.term.toDays}, ${request.requestTime}, ${request.clientIp}, false)
      """.execute()

      StoredLoanRequest(id, request, approved = false)
    }
  }

  override def updateApproved(request: StoredLoanRequest): StoredLoanRequest = {
    db.withConnection { implicit conn =>
      SQL"""
        UPDATE loans SET approved = ${request.approved} WHERE id = ${request.id.toString}
      """.executeUpdate().
        ensuring(_ == 1, "Expected update on ONE row")

      request
    }
  }

  override def countByClientIP(clientIP: String, forDate: DateTime): Int = {
    db.withConnection { implicit conn =>

      // this allows code to be independent of DB syntax
      val maxTime = forDate.hourOfDay().withMaximumValue()
      val minTime = forDate.hourOfDay().withMinimumValue()

      SQL"""
        SELECT COUNT(*) FROM loans WHERE client_ip = $clientIP AND timestamp BETWEEN $minTime AND $maxTime
      """.as(SqlParser.scalar[Int].single)

    }
  }

  override def getByID(id: UUID): StoredLoanRequest = {
    db.withConnection { implicit conn =>
      SQL"""
        SELECT *
        FROM loans
        WHERE id = ${id.toString}
      """.as(approvalParser.single)
    }
  }
}

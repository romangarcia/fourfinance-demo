package model

import java.util.UUID

import controllers.LoansController
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsValue, Json}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}

class LoansControllerSpec extends FlatSpec with Matchers with DefaultAwaitTimeout {
  import Helpers._
  import TestLoans._
  import TestLoanRisk._

  "Loans Controller" should "reject loan with 200-OK response" in {

    // high risk with risky hour with a huge amount
    val calendar = FakeCalendar(timestampAtHour(4))
    val jsonReq = jsonLoanRequest(amount = 5000.99)

    withLoanController(calendar) { ctrlr =>
      val request = FakeRequest().withJsonBody(jsonReq)
      val response = ctrlr.requestLoan.apply(request)

      assert( status(response) === OK )
      val js = contentAsJson(response)

      assert( (js \ "success").as[Boolean] === false )
      assert( (js \ "error").as[String] === "rejection" )
      assert( (js \ "reason").as[String].nonEmpty )
    }

  }

  it should "approve loan with 200-OK response" in {

    // expect low risk (early hour, but non-maximum amount)
    val calendar = FakeCalendar(timestampAtHour(2))
    val jsonReq = jsonLoanRequest(amount = 4999.99)

    withLoanController(calendar) { ctrlr =>
      val request = FakeRequest().withJsonBody(jsonReq)
      val response = ctrlr.requestLoan.apply(request)

      assert( status(response) === OK )
      val js = contentAsJson(response)

      assert( (js \ "success").as[Boolean] === true )
      assert( (js \ "error").asOpt[String].isEmpty )
      assert( (js \ "loan_id").asOpt[UUID].nonEmpty )
    }

  }

  it should "fail with 500-ISE when json request is invalid" in {

    // expect low risk (early hour, but non-maximum amount)
    val calendar = FakeCalendar(timestampAtHour(2))

    // invalid json, no amount...
    val jsonReq = Json.parse(
      s"""
         |{
         |    "first_name" : "Roman",
         |    "last_name" : "Garcia",
         |    "term" : "5 days"
         |}
      """.stripMargin)

    withLoanController(calendar) { ctrlr =>
      val request = FakeRequest().withJsonBody(jsonReq)
      val response = ctrlr.requestLoan.apply(request)

      assert( status(response) === INTERNAL_SERVER_ERROR )
      val js = contentAsJson(response)

      assert( (js \ "success").as[Boolean] === false )
      assert( (js \ "error").asOpt[JsValue].nonEmpty )
    }

  }

  it should "fail with 400-BadRequest on NON-JSON request" in {

    // expect low risk (early hour, but non-maximum amount)
    val calendar = FakeCalendar(timestampAtHour(2))

    withLoanController(calendar) { ctrlr =>
      val request = FakeRequest().withTextBody("This is not a JSON...")
      val response = ctrlr.requestLoan.apply(request)

      assert( status(response) === BAD_REQUEST )
      val js = contentAsJson(response)

      println(js)
      assert( (js \ "success").as[Boolean] === false )
      assert( (js \ "error").asOpt[JsValue].nonEmpty )
    }

  }

  def withLoanController(calendar: Calendar = FakeCalendar())(t: LoansController => Unit) = {

    val repo = new FakeLoansRepository

    val riskAnalysis = new DefaultLoanRiskAnalysis(repo, maximumAmount = 5000.0, 3)

    withLoans(repo, riskAnalysis) { loans =>
      val ctrlr = new LoansController(loans, calendar)
      t(ctrlr)
    }

  }

  def jsonLoanRequest(amount: BigDecimal): JsValue = {
    Json.parse(
      s"""
        |{
        |    "first_name" : "Roman",
        |    "last_name" : "Garcia",
        |    "amount" : $amount,
        |    "term" : "5 days"
        |}
      """.stripMargin)
  }
}

case class FakeCalendar(now: DateTime = DateTime.now) extends Calendar

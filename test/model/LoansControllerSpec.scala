package model

import controllers.LoansController
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}

class LoansControllerSpec extends FlatSpec with Matchers with DefaultAwaitTimeout {
  import Helpers._
  import TestLoans._
  import TestLoanRisk._

  "Loans Controller" should "reject loan with 200-OK response" in {

    // force high risk with risky hour with a huge amount
    val calendar = FakeCalendar(timestampAtHour(4))
    val jsonReq = Json.parse(
      """
        |{
        |    "first_name" : "Roman",
        |    "last_name" : "Garcia",
        |    "amount" : 5000.99,
        |    "term" : "5 days"
        |}
      """.stripMargin)

    withLoanController(calendar) { ctrlr =>
      val request = FakeRequest().withJsonBody(jsonReq)
      val response = ctrlr.requestLoan.apply(request)

      assert( status(response) === OK )
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
}

case class FakeCalendar(now: DateTime = DateTime.now) extends Calendar

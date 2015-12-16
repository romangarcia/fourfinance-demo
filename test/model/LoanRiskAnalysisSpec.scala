package model

import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

import scala.util.Random

class LoanRiskAnalysisSpec extends FlatSpec with Matchers {
  import TestLoanRisk._

  "RiskAnalysis" should
    "indicate high risk when request between 00:00 and 06:00 AM with maximum possible amount" in {

    assertRiskAnalysis(5000.00, hour = 1) {
      case LoanRiskLevels.HighRisk(msg) if msg contains "Maximum amount on early hours" =>
    }

  }

  it should "indicate low risk when request is for maximum amount but after 06:00AM" in {
    assertRiskAnalysis(5000.00, hour = 7) {
      case LoanRiskLevels.LowRisk =>
    }
  }

  it should "indicate low risk when request is between 00:00 and 06:00AM but not for maximum amount" in {
    assertRiskAnalysis(1000.00, hour = 1) {
      case LoanRiskLevels.LowRisk =>
    }
  }

  it should "indicate high risk when request is the fourth request same day" in {

    val repo = new FakeLoansRepository()
    saveRandomLoanRequests(qty = 3, timestampHour = 8, repo = repo)

    assertRiskAnalysis(1000.00, hour = 8, store = repo) {
      case LoanRiskLevels.HighRisk(msg) if msg contains "Exceeds per-day requests" =>
    }
  }

  it should "indicate low risk when request is the third request same day" in {

    val repo = new FakeLoansRepository()
    saveRandomLoanRequests(qty = 2, timestampHour = 8, repo = repo)

    assertRiskAnalysis(1000.00, hour = 8, store = repo) {
      case LoanRiskLevels.LowRisk =>
    }
  }

}

object TestLoanRisk {
  import scala.concurrent.duration._

  def saveRandomLoanRequests(qty: Int, timestampHour: Int, repo: LoansRepository): LoansRepository = {
    val clientIP = "127.0.0.1"
    val timestamp = timestampAtHour(timestampHour)

    (0 to qty).foreach { i =>
      val req = LoanRequest("Roman", "Garcia", Random.nextInt(5000), (i + 1).days, timestamp, clientIP)
      repo.add(req)
    }

    repo
  }

  def assertRiskAnalysis(amount: BigDecimal, hour: Int,
                         store: LoansRepository = new FakeLoansRepository(), maximumAmount: BigDecimal = 5000.00, maxRequestPerIP: Int = 3)
                        (t: PartialFunction[LoanRiskLevels.LoanRisk, Unit]) = {

    val timestamp = timestampAtHour(hour)
    val sampleRequest: LoanRequest = LoanRequest("Roman", "Garcia", amount, 5.days, timestamp, "127.0.0.1")
    val riskAnalysis = new DefaultLoanRiskAnalysis(store, maximumAmount, maxRequestPerIP)

    val analysisResult = riskAnalysis.analyze(sampleRequest)

    assert(t.isDefinedAt(analysisResult), s"Risk analysis was $analysisResult")

  }

  def timestampAtHour(hour: Int): DateTime = {
    DateTime.now().hourOfDay().withMinimumValue().plusHours(hour)
  }

}

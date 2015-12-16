package model

import model.LoanRiskLevels._
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

class LoansSpec extends FlatSpec with Matchers {
  import scala.concurrent.duration._
  import TestLoans._

  val sampleRequest: LoanRequest = LoanRequest("Roman", "Garcia", 33333.0, 5.days, DateTime.now, "127.0.0.1")

  "Loans" should "approve low-risk request" in {
    withLoans() { loans =>
      val loan = loans.approve(sampleRequest)
      assert( loan.approved )
    }
  }

  it should "persist requested loan" in {
    val store = new FakeLoansRepository()
    withLoans(store) { loans =>
      val _ = loans.approve(sampleRequest)
      assert( store.loans.size == 1 )
    }
  }

  it should "reject high-risk loan" in {
    val riskAnalysis = new FakeLoanRiskAnalysis(HighRisk("Fake High Risk"))
    withLoans(riskAnalysis = riskAnalysis) { loans =>
      val ex = intercept[LoanRejectedException] {
        loans.approve(sampleRequest)
      }

      assert( ex.getMessage === "Fake High Risk" )
    }
  }

}

object TestLoans {

  def withLoans(store: LoansRepository = new FakeLoansRepository(),
                riskAnalysis: LoanRiskAnalysis = new FakeLoanRiskAnalysis(LowRisk))(t: Loans => Unit) = {
    val loans = new DefaultLoans(riskAnalysis, store)
    t(loans)
  }

}
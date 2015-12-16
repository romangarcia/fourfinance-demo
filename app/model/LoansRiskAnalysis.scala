package model

object LoanRiskLevels {
  sealed trait LoanRisk
  case object LowRisk extends LoanRisk
  case class HighRisk(msg: String) extends LoanRisk

}

trait LoanRiskAnalysis {
  def analyze(request: LoanRequest): LoanRiskLevels.LoanRisk
}

class DefaultLoanRiskAnalysis(store: LoansRepository, maximumAmount: BigDecimal, maximumRequestPerIP: Int) extends LoanRiskAnalysis {
  import LoanRiskLevels._

  override def analyze(request: LoanRequest): LoanRisk = {

    if (isWithinRiskHours(request) && isMaximumAmount(request)) {
      HighRisk("Maximum amount on early hours")
    } else if (exceedsRequestsPerClientIP(request)) {
      HighRisk("Exceeds per-day requests")
    } else {
      LowRisk
    }
  }

  private def exceedsRequestsPerClientIP(request: LoanRequest): Boolean = {
    store.countByClientIP(request.clientIp, request.requestTime) > maximumRequestPerIP
  }

  private def isMaximumAmount(request: LoanRequest): Boolean = {
    request.amount >= maximumAmount
  }

  private def isWithinRiskHours(request: LoanRequest): Boolean = {
    request.requestTime.getHourOfDay >= 0 && request.requestTime.getHourOfDay <= 6
  }
}

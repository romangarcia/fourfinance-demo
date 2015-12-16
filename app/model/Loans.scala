package model

trait Loans {
  def approve(request: LoanRequest): StoredLoanRequest
}

class DefaultLoans(riskAnalysis: LoanRiskAnalysis, store: LoansRepository) extends Loans {
  import play.api.Logger._

  override def approve(request: LoanRequest): StoredLoanRequest = {
    import LoanRiskLevels._

    val storedReq = store.add(request)
    info(s"Saved Loan Request [${storedReq.id}. Approval pending...")

    riskAnalysis.analyze(request) match {
      case LowRisk =>
        info(s"Loan Request [${storedReq.id}] was LOW RISK...APPROVED!")
        store.updateApproved(storedReq.copy(approved = true))
      case HighRisk(msg) =>
        info(s"Loan Request [${storedReq.id}] was HIGH RISK...REJECTED!")
        throw new LoanRejectedException(msg)
    }

  }

}

package model

import java.util.UUID

import org.joda.time.DateTime

class FakeLoansRepository() extends LoansRepository {
  var loans: Map[UUID, StoredLoanRequest] = Map()

  override def add(request: LoanRequest): StoredLoanRequest = {
    val id = UUID.randomUUID()
    val storedLoanRequest = StoredLoanRequest(id, request, approved = false)
    loans = loans.updated(id, storedLoanRequest)
    storedLoanRequest
  }

  override def getByID(id: UUID): StoredLoanRequest = loans(id)

  override def countByClientIP(clientIP: String, forDate: DateTime): Int = {
    loans.values.count(l =>
      l.request.clientIp == clientIP &&
        forDate.toLocalDate == l.request.requestTime.toLocalDate)
  }

  override def updateApproved(request: StoredLoanRequest): StoredLoanRequest = {
    loans = loans.updated(request.id, request)
    request
  }
}

class FakeLoanRiskAnalysis(analysis: => LoanRiskLevels.LoanRisk) extends LoanRiskAnalysis {
  override def analyze(request: LoanRequest): LoanRiskLevels.LoanRisk = analysis
}

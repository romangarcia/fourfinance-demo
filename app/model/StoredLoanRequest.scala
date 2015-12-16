package model

import java.util.UUID

case class StoredLoanRequest(id: UUID, request: LoanRequest, approved: Boolean)

class LoanRejectedException(reason: String) extends Exception(reason)


package controllers

import model.{Calendar, LoanRejectedException, LoanRequest, Loans}
import play.api.Logger
import play.api.libs.json.{Json, JsError, JsSuccess}
import play.api.mvc.{RequestHeader, Action, Controller}

import scala.util.Try

class LoansController(loans: Loans, calendar: Calendar) extends Controller {

  def requestLoan = Action { implicit request =>
    request.body.asJson.map(_.validate[LoanRequestJson] match {
      case JsSuccess(loanReqMsg, _) =>
        Try {
          Logger.info(s"Received a Loan Request [$loanReqMsg]")
          val loanReq = newRequest(loanReqMsg)
          Logger.info(s"Requesting approval on Loan Request [$loanReq]")
          val approvedReq = loans.approve(loanReq)
          Ok(Json.obj("success" -> false, "loan_id" -> approvedReq.id))
        }.recover {
          case e: LoanRejectedException =>
            Ok(Json.obj("success" -> false, "error" -> "rejection", "reason" -> e.getMessage))
          case e: Exception =>
            InternalServerError(Json.obj("success" -> false, "error" -> "rejection", "reason" -> e.getMessage))
        }.get
      case err: JsError =>
        InternalServerError(Json.obj("success" -> false, "error" -> JsError.toJson(err)))
    }).getOrElse {
      BadRequest(Json.obj("success" -> false, "error" -> "Expected Json request"))
    }
  }

  def newRequest(json: LoanRequestJson)(implicit request: RequestHeader): LoanRequest = {
    LoanRequest(json.firstName, json.lastName, json.amount, json.term, calendar.now, request.remoteAddress)
  }

}

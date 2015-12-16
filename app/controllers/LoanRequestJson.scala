package controllers

import scala.concurrent.duration.Duration

object LoanRequestJson {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val loanRequestReads: Reads[LoanRequestJson] = (
    (__ \ "first_name").read[String] and
    (__ \ "last_name").read[String] and
    (__ \ "amount").read[BigDecimal] and
    (__ \ "term").read[String].map(Duration.apply)
  )(LoanRequestJson.apply _)

}

case class LoanRequestJson(firstName: String,
                       lastName: String,
                       amount: BigDecimal,
                       term: Duration)




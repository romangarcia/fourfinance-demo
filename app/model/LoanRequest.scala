package model

import org.joda.time.DateTime

import scala.concurrent.duration.Duration

case class LoanRequest(firstName: String,
                       lastName: String,
                       amount: BigDecimal,
                       term: Duration,
                       requestTime: DateTime,
                       clientIp: String)

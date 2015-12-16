package boot

import com.softwaremill.macwire._
import controllers.LoansController
import model._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.{HikariCPComponents, DBComponents}
import play.api.routing.Router
import router.Routes

class LoansApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    (new BuiltInComponentsFromContext(context) with LoansComponents).application
  }
}

trait LoansComponents extends BuiltInComponents with DBComponents with HikariCPComponents with EvolutionsComponents {
  lazy val router: Router = {
    lazy val prefix = "/"
    wire[Routes]
  }

  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  lazy val loansStore = wire[JdbcLoansRepository]
  lazy val riskAnalysis = wireWith(RiskAnalysisFactory.create _)
  lazy val loans = wire[DefaultLoans]
  lazy val calendar = DefaultCalendar
  lazy val loansController = wire[LoansController]

  // this hack is to allow evolutions to run (no documentation available)
  applicationEvolutions
}

object RiskAnalysisFactory {
  val maximumLoanAmount = 5000.0
  val maximumLoanRequestsPerDay = 3

  def create(store: LoansRepository): LoanRiskAnalysis =
    new DefaultLoanRiskAnalysis(store, maximumLoanAmount, maximumLoanRequestsPerDay)
}
package uk.gov.hmrc.apiplatformapicataloguepublish.support


import akka.stream.Materializer
import org.scalatestplus.play.PlaySpec
import org.scalatest._
import org.scalatestplus.play.WsScalaTestClient
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter


abstract class BaseISpec
  extends  WordSpec  with OptionValues with WsScalaTestClient with WireMockSupport with MetricsTestSupport with Matchers {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  override def commonStubs(): Unit = {
    givenCleanMetricRegistry()
  }

  protected implicit def materializer: Materializer = app.materializer

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
}

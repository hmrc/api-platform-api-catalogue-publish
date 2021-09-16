package uk.gov.hmrc.apiplatformapicataloguepublish.support


import akka.stream.Materializer
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter


abstract class BaseISpec
  extends PlaySpec with WireMockSupport with MetricsTestSupport with MustMatchers {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  override def commonStubs(): Unit = {
    givenCleanMetricRegistry()
  }

  protected implicit def materializer: Materializer = app.materializer

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
}

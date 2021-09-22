package uk.gov.hmrc.apiplatformapicataloguepublish.support


import akka.stream.Materializer
import org.scalatest.OptionValues
import org.scalatestplus.play.{PlaySpec, WsScalaTestClient}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter


abstract class BaseISpec
  extends  PlaySpec  with OptionValues with WsScalaTestClient with WireMockSupport with MetricsTestSupport {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  override def commonStubs(): Unit = {
    givenCleanMetricRegistry()
  }

  protected implicit def materializer: Materializer = app.materializer

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
}

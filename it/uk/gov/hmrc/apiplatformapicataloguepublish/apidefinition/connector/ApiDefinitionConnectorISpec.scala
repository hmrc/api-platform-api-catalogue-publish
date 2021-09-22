package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{ApiDefinitionStub, MetricsTestSupport, ServerBaseISpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream4xxResponse, UpstreamErrorResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinitionJsonFormatters
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiVersion._
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData

class ApiDefinitionConnectorISpec
    extends ServerBaseISpec
    with ApiDefinitionStub
    with ApiDefinitionBuilder
    with ApiDefinitionJsonFormatters
    with ApiDefinitionData  with BeforeAndAfterEach with MetricsTestSupport{

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> true,
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.host" -> wireMockHost,
        "auditing.consumer.baseUri.port" -> wireMockPort,
        "microservice.services.api-definition.host" -> wireMockHost,
        "microservice.services.api-definition.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val objInTest: ApiDefinitionConnector = app.injector.instanceOf[ApiDefinitionConnector]
  }

  "ApiDeifintionConnector" should {
    "returns an api definition" in new Setup {
      val jsonBody = Json.toJson(apiDefinition1).toString
      primeGetByServiceName(
        OK,
        jsonBody,
        serviceName
      )
      val result =
        await(objInTest.getDefinitionByServiceName(serviceName)) match {
          case Right(x: ApiDefinition) => x mustBe apiDefinition1
          case _                      => fail

        }
    }
    "return an exception" in new Setup {
      primeGetByServiceName(
        BAD_REQUEST,
        Json.toJson(apiDefinition1).toString,
        serviceName
      )

       val result =  await(objInTest.getDefinitionByServiceName(serviceName))
      result match {
        case Left(e: uk.gov.hmrc.http.Upstream4xxResponse) => succeed
        case _ => fail()
      }

    }
  }
}

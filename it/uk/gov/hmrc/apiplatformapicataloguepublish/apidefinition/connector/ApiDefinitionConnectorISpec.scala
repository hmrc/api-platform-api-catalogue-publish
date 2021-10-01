package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.{ApiDefinitionGeneralFailedResult, ApiDefinitionNotFoundResult, ApiDefinitionResult}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinitionJsonFormatters
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.{ApiDefinitionBuilder, ApiDefinitionUtils}
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{ApiDefinitionStub, MetricsTestSupport, ServerBaseISpec}
import uk.gov.hmrc.http.HeaderCarrier

class ApiDefinitionConnectorISpec
  extends ServerBaseISpec
    with ApiDefinitionStub
    with ApiDefinitionBuilder
    with ApiDefinitionJsonFormatters
    with ApiDefinitionData
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiDefinitionUtils {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false,
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

  "ApiDefinitionConnector" should {
    "returns an api definition" in new Setup {
      val expectedResult: ApiDefinitionResult =
        ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), apiDefinition1.serviceName)
      val jsonBody: String = Json.toJson(apiDefinition1).toString
      primeGetByServiceName(
        OK,
        jsonBody,
        serviceName
      )
      await(objInTest.getDefinitionByServiceName(serviceName)) match {
        case Right(x: ApiDefinitionResult) => x mustBe expectedResult
        case _ => fail

      }
    }

    "returns a Left ApiDefinitionNotFoundResult when not found returned" in new Setup {
      primeGetByServiceName(
        NOT_FOUND,
        "{}",
        serviceName
      )
      await(objInTest.getDefinitionByServiceName(serviceName)) match {
        case Left(_: ApiDefinitionNotFoundResult) => succeed
        case _ => fail

      }
    }
    "returns a Left ApiDefinitionBadGatewayResult when bad gateway returned" in new Setup {
      primeGetByServiceName(
        BAD_GATEWAY,
        "{}",
        serviceName
      )
      await(objInTest.getDefinitionByServiceName(serviceName)) match {
        case Left(_: ApiDefinitionGeneralFailedResult) => succeed
        case _ => fail

      }
    }


  }
}

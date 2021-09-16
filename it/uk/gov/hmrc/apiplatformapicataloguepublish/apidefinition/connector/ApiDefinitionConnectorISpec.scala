package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector

import uk.gov.hmrc.apiplatformapicataloguepublish.support.{
  ApiDefinitionStub,
  ServerBaseISpec
}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinitionJsonFormatters
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiVersion._
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.http.UpstreamErrorResponse

class ApiDefinitionConnectorISpec
    extends ServerBaseISpec
    with ApiDefinitionStub
    with ApiDefinitionBuilder
    with ApiDefinitionJsonFormatters {

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

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val objInTest: ApiDefinitionConnector =
      app.injector.instanceOf[ApiDefinitionConnector]

    val categories = List(ApiCategory("category1"), ApiCategory("category2"))

    val apiDefinition1 = apiDefinition("my-service")
          .withVersions(apiVersion(version = ApiVersion.random), apiVersion(version = ApiVersion.random))
      .withCategories(categories)
  }

  "ApiDeifintionConnector" should {
    "returns an api definition" in new Setup {
      val serviceName = "my-service"
      primeGetByServiceName(
        OK,
        Json.toJson(apiDefinition1).toString,
        serviceName
      )
      val result =
        await(objInTest.getDefinitionByServiceName(serviceName)) match {
          case Some(x: ApiDefinition) => x shouldBe apiDefinition1
          case _                      => fail

        }
    }
    "return an exception" in new Setup {
      val serviceName = "my-service"
      primeGetByServiceName(
        BAD_REQUEST,
        Json.toJson(apiDefinition1).toString,
        serviceName
      )
      intercept[UpstreamErrorResponse] {
        await(objInTest.getDefinitionByServiceName(serviceName))
      }.statusCode shouldBe BAD_REQUEST

    }
  }
}

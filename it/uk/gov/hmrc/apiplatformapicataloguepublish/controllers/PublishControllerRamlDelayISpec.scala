package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import com.typesafe.play.cachecontrol.ResponseSelectionActions.GatewayTimeout
import org.scalatest.BeforeAndAfterEach
import org.scalatest.time.{Seconds, Span}
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{NOT_IMPLEMENTED, OK}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.support._
import webapi.{Raml10, WebApiDocument}

import org.scalatest.time.{Millis, Seconds, Span}

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.io.Source

class PublishControllerRamlDelayISpec
    extends ServerBaseISpec
    with AwaitTestSupport
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiDefinitionStub
    with ApiProducerTeamStub
    with ApiDefinitionData
    with ApiDefinitionJsonFormatters
    with ApiDefinitionUtils
    with ApiCatalogueStub
    with ApiCatalogueAdminJsonFormatters {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> true,
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.host" -> wireMockHost,
        "auditing.consumer.baseUri.port" -> wireMockPort,
        "microservice.services.api-definition.host" -> wireMockHost,
        "microservice.services.api-definition.port" -> wireMockPort,
        "microservice.services.integration-catalogue-admin-api.host" -> wireMockHost,
        "microservice.services.integration-catalogue-admin-api.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()

  }

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(120, Seconds), interval = Span(500, Millis))

  val url = s"http://localhost:$port/api-platform-api-catalogue-publish"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val validHeaders = List(CONTENT_TYPE -> "application/json")

  def callPostEndpoint(url: String, body: String, headers: List[(String, String)]): WSResponse =
    wsClient
      .url(url)
      .withHttpHeaders(headers: _*)
      .withFollowRedirects(false)
      .post(body)
      .futureValue

  trait Setup {
    def addWiremockToDefinitionUrl(apiDefinition: ApiDefinition): ApiDefinition ={
      apiDefinition.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition.serviceBaseUrl}")
    }

    val apiDefinition1: ApiDefinition = createApiDefinition("definition1a", "definition1", List("1.0", "2.0", "3.0"))
    val apiDefinition1withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition1)

    val apiDefinition2: ApiDefinition = createApiDefinition("definition2b", "definition2", List("2.0", "3.0", "4.0"))
    val apiDefinition2withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition2)

    val apiDefinition3: ApiDefinition = createApiDefinition("definition3c", "definition3", List("3.0", "4.0", "5.0"))
    val apiDefinition3withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition3)

    val apiDefinition4: ApiDefinition = createApiDefinition("definition4d", "definition4", List("4.0", "5.0", "6.0"))
    val apiDefinition4withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition4)

    val apiDefinition5: ApiDefinition = createApiDefinition("definition5e", "definition5", List("5.0", "6.0", "7.0"))
    val apiDefinition5withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition5)

    val apiDefinition6: ApiDefinition = createApiDefinition("definition6f", "definition6", List("6.0", "7.0", "8.0"))
    val apiDefinition6withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition6)

    val apiDefinition7: ApiDefinition = createApiDefinition("definition7g", "definition7", List("6.0", "7.0", "8.0"))
    val apiDefinition7withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition7)

    val apiDefinition8: ApiDefinition = createApiDefinition("definition8h", "definition8", List("6.0", "7.0", "8.0"))
    val apiDefinition8withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition8)

    val apiDefinition9: ApiDefinition = createApiDefinition("definition9f", "definition9", List("6.0", "7.0", "8.0"))
    val apiDefinition9withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition9)

    val apiDefinition10: ApiDefinition = createApiDefinition("definition10f", "definition10", List("6.0", "7.0", "8.0"))
    val apiDefinition10withWiremock: ApiDefinition = addWiremockToDefinitionUrl(apiDefinition10)

    val apiDefinitionListAsString: String = Json.toJson(List(apiDefinition1withWiremock,
      apiDefinition2withWiremock,
      apiDefinition3withWiremock,
      apiDefinition4withWiremock,
      apiDefinition5withWiremock,
      apiDefinition6withWiremock,
      apiDefinition7withWiremock,
      apiDefinition8withWiremock,
      apiDefinition9withWiremock,
      apiDefinition10withWiremock)).toString

    val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
    val publishResponseAsJsonString: String = Json.toJson(publishResponse).toString

    primeGetAll(OK, apiDefinitionListAsString)

    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition1), absoluteRamlFilePath, 8000)
    primeGETReturnsErrorWithDelay("/" + getRamlUri(apiDefinition2), NOT_IMPLEMENTED)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition3), absoluteRamlFilePath, 7500)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition4), absoluteRamlFilePath, 6000)
    primeGETReturnsErrorWithDelay("/" + getRamlUri(apiDefinition5), NOT_IMPLEMENTED)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition6), absoluteRamlFilePath, 3000)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition7), absoluteRamlFilePath, 5000)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition8), absoluteRamlFilePath, 4000)
    primeGETReturnsErrorWithDelay("/" + getRamlUri(apiDefinition9), NOT_IMPLEMENTED)
    primeGETWithFileContentsAndRandomDelay("/" + getRamlUri(apiDefinition10), absoluteRamlFilePath, 1500)

    primeApiPublish(publishResponseAsJsonString, OK)
    def callPublishEndpoint(serviceName: String): WSResponse = {
      callPostEndpoint(s"$url/publish/$serviceName", body = "", List.empty)
    }

    def callPublishAllEndpoint(): WSResponse = {
      callPostEndpoint(s"$url/publish-all", body = "", List.empty)
    }

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def absoluteRamlFilePath: String = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "it/resources/test-ramlFile.raml"

  }

  "PublishController Raml Delay Spike" when {



    "POST /publish-all" should {


      "respond with 200 when publish is successful" in new Setup {


        val result: WSResponse = callPublishAllEndpoint()
        result.status mustBe OK
        println(s"TEST FINISHED: ${result.body}")
        result.body mustBe """{"successCount":7,"failureCount":3}"""
      }
    }
  }
}

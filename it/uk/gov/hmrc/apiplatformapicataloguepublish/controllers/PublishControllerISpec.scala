package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{BAD_REQUEST, NOT_FOUND, OK}
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{AwaitTestSupport, MetricsTestSupport, ServerBaseISpec}

import scala.collection.immutable
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration._
import webapi.{WebApiDocument, Raml10}
import scala.io.Source
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{ApiDefinitionStub, ApiProducerTeamStub}
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._

import java.nio.file.Paths

class PublishControllerISpec extends ServerBaseISpec  with AwaitTestSupport with BeforeAndAfterEach with MetricsTestSupport
with ApiDefinitionStub with ApiProducerTeamStub with ApiDefinitionData with ApiDefinitionJsonFormatters {


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

  val url = s"http://localhost:$port/api-platform-api-catalogue-publish/publish"

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

      def callPublishEndpoint(serviceName: String)={
        callPostEndpoint(s"$url/$serviceName", body= "", List.empty)
      }

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def absoluteRamlFilePath =  Paths.get(".").toAbsolutePath().toString().replace(".", "") + "it/resources/test-ramlFile.raml"

  }

  "PublishController" when {

    "POST /publish/[serviceName]" should {
      "respond with 200 " in new Setup {
        val serviceName = "my-service"
        val apiDefinitionAsString = Json.toJson(apiDefinition1).toString
         primeGetByServiceName(OK, apiDefinitionAsString,  serviceName )
        primeGETWithFileContents(ApiDefinition.getRamlUri(apiDefinition1) , absoluteRamlFilePath, OK)
        val result: WSResponse = callPublishEndpoint(serviceName)
        result.status shouldBe OK
        
      }
    }
  }
}

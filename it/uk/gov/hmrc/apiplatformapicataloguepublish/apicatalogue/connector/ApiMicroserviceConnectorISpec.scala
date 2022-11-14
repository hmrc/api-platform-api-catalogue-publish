package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.ApiCatalogueAdminJsonFormatters
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{ApiMicroserviceStub, MetricsTestSupport, ServerBaseISpec}
import uk.gov.hmrc.http.HeaderCarrier

class ApiMicroserviceConnectorISpec
  extends ServerBaseISpec
    with ApiMicroserviceStub
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiCatalogueAdminJsonFormatters {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false,
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

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val objInTest: ApiMicroserviceConnector = app.injector.instanceOf[ApiMicroserviceConnector]
  }

  "ApiMicroserviceConnector" should {
    val filePath = "it/resources/test-yaml-file.yaml"
    val largeFilePath = "it/resources/test-large-yaml-file.yaml"
    val path = "/api/1/resource.yaml"
    val microserviceUrl = s"http://$wireMockHost:$wireMockPort"+path

    "returns a Right if call to microservice returns OK with a small file" in new Setup {


      primeFetchResource(
        path,
        filePath,
        OK
      )

      val result = await(objInTest.fetchApiDocumentationResourceByUrl(microserviceUrl))
      result match {
        case Right(_: String) => succeed
        case _ => fail
      }
    }

    "returns a Right if call to microservice returns OK with a large file" in new Setup {

      primeFetchResource(
        path,
        largeFilePath,
        OK
      )

      val result = await(objInTest.fetchApiDocumentationResourceByUrl(microserviceUrl))
      result match {
        case Right(_: String) => succeed
        case _ => fail
      }
    }

    "returns a Left with NotFoundException when 404 returned from microservice" in new Setup {
      primeFetchResource(
        path,
        filePath,
        NOT_FOUND
      )
      val result = await(objInTest.fetchApiDocumentationResourceByUrl(microserviceUrl))
      result match {
        case Left(_) => succeed
        case _ => fail
      }
    }

    "returns a Left with InternalServerException when 500 returned from microservice" in new Setup {
      primeFetchResource(
        path,
        filePath,
        INTERNAL_SERVER_ERROR
      )
      val result = await(objInTest.fetchApiDocumentationResourceByUrl(microserviceUrl))
      result match {
        case Left(_) => succeed
        case _ => fail
      }
    }

  }

}

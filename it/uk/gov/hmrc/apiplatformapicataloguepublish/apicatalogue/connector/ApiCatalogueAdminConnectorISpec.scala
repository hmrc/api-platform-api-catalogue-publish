/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueGeneralFailureResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.support.{ApiCatalogueStub, MetricsTestSupport, ServerBaseISpec}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID

class ApiCatalogueAdminConnectorISpec
    extends ServerBaseISpec
    with ApiCatalogueStub
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiCatalogueAdminJsonFormatters {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                                            -> false,
        "auditing.enabled"                                           -> false,
        "auditing.consumer.baseUri.host"                             -> wireMockHost,
        "auditing.consumer.baseUri.port"                             -> wireMockPort,
        "microservice.services.api-definition.host"                  -> wireMockHost,
        "microservice.services.api-definition.port"                  -> wireMockPort,
        "microservice.services.integration-catalogue-admin-api.host" -> wireMockHost,
        "microservice.services.integration-catalogue-admin-api.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val objInTest: ApiCatalogueAdminConnector = app.injector.instanceOf[ApiCatalogueAdminConnector]
  }

  "ApiCatalogueAdminConnector" should {
    "returns a Right with PublishResponse when successful" in new Setup {
      val response: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
      val jsonBody: String          = Json.toJson(response).toString

      primeApiPublish(
        jsonBody,
        OK
      )
      val result: Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse] = await(objInTest.publishApi("serviceName"))
      result match {
        case Right(response: PublishResponse) => succeed
        case _                                => fail
      }
    }

    "returns a Left with ApiCatalogue4xxFailureResult when 400 error returned from connector" in new Setup {
      primeApiPublish(
        "{}",
        NOT_FOUND
      )
      val result: Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse] = await(objInTest.publishApi("serviceName"))
      result match {
        case Left(response: ApiCatalogueGeneralFailureResult) => succeed
        case _                                                => fail
      }
    }

    "returns a Left with ApiCatalogue5xxFailureResult when 500 error returned from connector" in new Setup {

      primeApiPublish(
        "{}",
        BAD_GATEWAY
      )
      val result: Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse] = await(objInTest.publishApi("serviceName"))
      result match {
        case Left(_: ApiCatalogueGeneralFailureResult) => succeed
        case _                                         => fail
      }
    }

    "returns a Left with ApiCatalogue5xxFailureResult when response body cannot be parsed" in new Setup {

      primeApiPublish(
        "{}",
        OK
      )
      val result: Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse] = await(objInTest.publishApi("serviceName"))
      result match {
        case Left(e: ApiCatalogueGeneralFailureResult) => e.message.startsWith("Js Parse Errors") mustBe true
        case _                                         => fail
      }
    }
  }

}

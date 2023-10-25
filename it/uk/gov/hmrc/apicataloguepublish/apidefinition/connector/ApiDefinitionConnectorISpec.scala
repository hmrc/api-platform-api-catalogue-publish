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

package uk.gov.hmrc.apicataloguepublish.apidefinition.connector

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.apicataloguepublish.apidefinition.connector.ApiDefinitionConnector.{ApiDefinitionResult, GeneralFailedResult, NotFoundResult}
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.{ApiDefinitionBuilder, ApiDefinitionUtils}
import uk.gov.hmrc.apicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apicataloguepublish.support.{ApiDefinitionStub, MetricsTestSupport, ServerBaseISpec}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ApiDefinition

class ApiDefinitionConnectorISpec
    extends ServerBaseISpec
    with ApiDefinitionStub
    with ApiDefinitionBuilder
    with ApiDefinitionData
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiDefinitionUtils {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                           -> false,
        "auditing.enabled"                          -> false,
        "auditing.consumer.baseUri.host"            -> wireMockHost,
        "auditing.consumer.baseUri.port"            -> wireMockPort,
        "microservice.services.api-definition.host" -> wireMockHost,
        "microservice.services.api-definition.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val definitionResult1: ApiDefinitionResult = buildResult(apiDefinition1)
    val definitionResult2: ApiDefinitionResult = buildResult(apiDefinition2)

    val objInTest: ApiDefinitionConnector = app.injector.instanceOf[ApiDefinitionConnector]
  }

  def buildResult(definition: ApiDefinition) = {
    ApiDefinitionResult(getUri(definition), getAccessTypeOfLatestVersion(definition), definition.serviceName, getStatusOfLatestVersion(definition))
  }

  "ApiDefinitionConnector" when {

    "getDefinitionByServiceName" should {
      "returns an api definition" in new Setup {

        val jsonBody: String = Json.toJson(apiDefinition1).toString
        primeGetByServiceName(
          OK,
          jsonBody,
          serviceName
        )
        await(objInTest.getDefinitionByServiceName(serviceName)) match {
          case Right(x: ApiDefinitionResult) => x mustBe definitionResult1
          case _                             => fail()

        }
      }

      "returns a Left ApiDefinitionNotFoundResult when not found returned" in new Setup {
        primeGetByServiceName(
          NOT_FOUND,
          "{}",
          serviceName
        )
        await(objInTest.getDefinitionByServiceName(serviceName)) match {
          case Left(_: NotFoundResult) => succeed
          case _                       => fail()

        }
      }
      "returns a Left ApiDefinitionBadGatewayResult when bad gateway returned" in new Setup {
        primeGetByServiceName(
          BAD_GATEWAY,
          "{}",
          serviceName
        )
        await(objInTest.getDefinitionByServiceName(serviceName)) match {
          case Left(_: GeneralFailedResult) => succeed
          case _                            => fail()

        }
      }

    }
  }
  "getAllServices" should {

    "returns right with list of definitions when successful" in new Setup {
      val jsonBody = Json.toJson(List(apiDefinition1, apiDefinition2)).toString
      primeGetAll(OK, jsonBody)
      await(objInTest.getAllServices()) match {
        case Left(_: GeneralFailedResult)              => fail()
        case Right(results: List[ApiDefinitionResult]) =>
          results mustBe List(definitionResult1, definitionResult2)

      }
    }

    "return right with empty list when no definitions returned" in new Setup {
      primeGetAll(OK, "[]")
      await(objInTest.getAllServices()) match {
        case Right(x: List[ApiDefinitionResult]) => x mustBe List.empty
        case x                                   => fail()
      }
    }

    "return left with error when error returned" in new Setup {
      primeGetAll(INTERNAL_SERVER_ERROR, "[]")
      await(objInTest.getAllServices()) match {
        case Left(x: GeneralFailedResult) =>
          x.message mustBe s"GET of 'http://localhost:$wireMockPort/api-definition?type=all' returned 500. Response body: '[]'"
        case _                            => fail()
      }
    }
  }
}

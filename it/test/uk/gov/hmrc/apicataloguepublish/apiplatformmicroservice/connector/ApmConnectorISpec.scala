/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector

import org.scalatest.BeforeAndAfterEach

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, OFormat}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.apis.domain.models.{ApiDefinition, Locator, ServiceName}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiVersionNbr, Environment}
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector.ApmConnector
import uk.gov.hmrc.apicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apicataloguepublish.support.{ApiPlatformMicroserviceStub, ServerBaseISpec}

class ApmConnectorISpec
    extends ServerBaseISpec
    with ApiPlatformMicroserviceStub
    with ApiDefinitionData
    with BeforeAndAfterEach
    with ApiDefinitionUtils {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                                      -> false,
        "auditing.enabled"                                     -> false,
        "auditing.consumer.baseUri.host"                       -> wireMockHost,
        "auditing.consumer.baseUri.port"                       -> wireMockPort,
        "microservice.services.api-platform-microservice.host" -> wireMockHost,
        "microservice.services.api-platform-microservice.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    implicit val locatorFormatter: OFormat[Locator[ApiDefinition]] = Locator.buildLocatorFormatter[ApiDefinition]

    val objInTest: ApmConnector = app.injector.instanceOf[ApmConnector]

    def buildResult(environment: Environment, definition: ApiDefinition): ApmConnector.Result = {
      ApmConnector.Result(environment, getAccessTypeOfLatestVersion(definition), definition.serviceName, getLatestVersion(definition), getStatusOfLatestVersion(definition))
    }
  }

  "fetchApiForServiceName" should {

    "returns an API definition from production" in new Setup {

      val definitionResult: ApmConnector.Result = buildResult(Environment.PRODUCTION, apiDefinition1)
      val locator: Locator[ApiDefinition]       = Locator.Production(apiDefinition1)
      val jsonBody: String                      = Json.toJson(locator).toString
      primeFetchApiForServiceName(
        OK,
        jsonBody,
        serviceName
      )

      await(objInTest.fetchApiForServiceName(serviceName)) match {
        case Right(x: ApmConnector.Result) => x shouldBe definitionResult
        case _                             => fail()
      }
    }

    "returns an API definition from sandbox" in new Setup {

      val definitionResult: ApmConnector.Result = buildResult(Environment.SANDBOX, apiDefinition1)
      val locator: Locator[ApiDefinition]       = Locator.Sandbox(apiDefinition1)
      val jsonBody: String                      = Json.toJson(locator).toString
      primeFetchApiForServiceName(
        OK,
        jsonBody,
        serviceName
      )

      await(objInTest.fetchApiForServiceName(serviceName)) match {
        case Right(x: ApmConnector.Result) => x shouldBe definitionResult
        case _                             => fail()
      }
    }

    "returns an API definition from production over sandbox" in new Setup {

      val definitionResult: ApmConnector.Result = buildResult(Environment.PRODUCTION, apiDefinition1)
      val locator: Locator[ApiDefinition]       = Locator.Both(sandboxValue = apiDefinition2, productionValue = apiDefinition1)
      val jsonBody: String                      = Json.toJson(locator).toString
      primeFetchApiForServiceName(
        OK,
        jsonBody,
        serviceName
      )

      await(objInTest.fetchApiForServiceName(serviceName)) match {
        case Right(x: ApmConnector.Result) => x shouldBe definitionResult
        case _                             => fail()
      }
    }

    "returns a Left ApiDefinitionNotFoundResult when not found returned" in new Setup {
      primeFetchApiForServiceName(
        NOT_FOUND,
        "{}",
        serviceName
      )
      await(objInTest.fetchApiForServiceName(serviceName)) match {
        case Left(_: ApmConnector.NotFoundResult) => succeed
        case _                                    => fail()

      }
    }
    "returns a Left ApiDefinitionBadGatewayResult when bad gateway returned" in new Setup {
      primeFetchApiForServiceName(
        BAD_GATEWAY,
        "{}",
        serviceName
      )
      await(objInTest.fetchApiForServiceName(serviceName)) match {
        case Left(_: ApmConnector.GeneralFailedResult) => succeed
        case _                                         => fail()
      }
    }
  }

  "fetchApiDocumentationResource" should {

    val environment = Environment.PRODUCTION
    val serviceName = ServiceName("api")
    val version     = ApiVersionNbr("1.0")
    val resource    = "resource.yaml"

    val filePath      = "test-yaml-file.yaml"
    val largeFilePath = "test-large-yaml-file.yaml"
    val path          = s"/environment/$environment/$serviceName/$version/documentation/$resource"

    "returns a Right if call to microservice returns OK with a small file" in new Setup {

      primeFetchApiDocumentationResource(
        path,
        filePath,
        OK
      )

      val result = await(objInTest.fetchApiDocumentationResource(environment, serviceName, version, resource))
      result match {
        case Right(_: String) => succeed
        case _                => fail()
      }
    }

    "returns a Right if call to microservice returns OK with a large file" in new Setup {

      primeFetchApiDocumentationResource(
        path,
        largeFilePath,
        OK
      )

      val result = await(objInTest.fetchApiDocumentationResource(environment, serviceName, version, resource))
      result match {
        case Right(_: String) => succeed
        case _                => fail()
      }
    }

    "returns a Left with NotFoundException when 404 returned from microservice" in new Setup {
      primeFetchApiDocumentationResource(
        path,
        filePath,
        NOT_FOUND
      )
      val result = await(objInTest.fetchApiDocumentationResource(environment, serviceName, version, resource))
      result match {
        case Left(_) => succeed
        case _       => fail()
      }
    }

    "returns a Left with InternalServerException when 500 returned from microservice" in new Setup {
      primeFetchApiDocumentationResource(
        path,
        filePath,
        INTERNAL_SERVER_ERROR
      )
      val result = await(objInTest.fetchApiDocumentationResource(environment, serviceName, version, resource))
      result match {
        case Left(_) => succeed
        case _       => fail()
      }
    }

  }

}

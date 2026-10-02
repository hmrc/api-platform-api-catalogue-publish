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

package uk.gov.hmrc.apicataloguepublish.service

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

import org.mockito.stubbing.ScalaOngoingStubbing
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import uk.gov.hmrc.apiplatform.modules.apis.domain.models.{ApiStatus, ServiceName}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiVersionNbr, Environment}
import uk.gov.hmrc.apicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector
import uk.gov.hmrc.apicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueGeneralFailureResult
import uk.gov.hmrc.apicataloguepublish.apicatalogue.models.{IntegrationId, PublishResponse}
import uk.gov.hmrc.apicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector.ApmConnector
import uk.gov.hmrc.apicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apicataloguepublish.openapi.OasResult
import uk.gov.hmrc.apicataloguepublish.parser.OasParser

class PublishServiceSpec
    extends AnyWordSpec
    with MockitoSugar
    with ArgumentMatchersSugar
    with Matchers
    with HeaderCarrierConverter
    with ApiDefinitionData
    with ScalaFutures
    with ApiDefinitionUtils {

  trait Setup {
    val mockConnector          = mock[ApiDefinitionConnector]
    val mockOasParser          = mock[OasParser]
    val mockCatalogueConnector = mock[ApiCatalogueAdminConnector]
    val mockApmConnector       = mock[ApmConnector]

    import java.nio.file.{Files, Paths}

    val filePath            = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "test/resources/noIntCatExtensions.yaml"
    val expectedOasFilePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "test/resources/expectedYamlConverted.yaml"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val yamlResponseString         = Files.readAllLines(Paths.get(filePath)).asScala.mkString
    val expectedEnhancedOasString  = Files.readAllLines(Paths.get(expectedOasFilePath)).asScala.mkString

    val apiDefinitionResult =
      ApmConnector.Result(Environment.PRODUCTION, getAccessTypeOfLatestVersion(apiDefinition1), serviceName, getLatestVersion(apiDefinition1), ApiStatus.STABLE)

    val apiDefinitionResult2                  =
      ApmConnector.Result(Environment.PRODUCTION, getAccessTypeOfLatestVersion(apiDefinition2), apiDefinition2.serviceName, getLatestVersion(apiDefinition2), ApiStatus.STABLE)
    val expectedDescription                   = "A description."
    val convertedWebApiToOasResult: OasResult = OasResult(expectedEnhancedOasString, serviceName, expectedDescription)
    val yamlOasResult: OasResult              = OasResult(yamlResponseString, serviceName, expectedDescription)
    val publishResponse: PublishResponse      = PublishResponse(IntegrationId(UUID.randomUUID()), "someRef")
    val ramlError: PublishFailedResult        = PublishFailedResult(serviceName, "YAML not found & RAML is no longer supported")

    val objInTest = new PublishService(mockConnector, mockOasParser, mockCatalogueConnector, mockApmConnector)

    def primeApiDefinitionSuccess(): ScalaOngoingStubbing[Future[Either[ApmConnector.FailedResult, ApmConnector.Result]]] = {
      when(mockApmConnector.fetchApiForServiceName(eqTo(serviceName))(*))
        .thenReturn(Future.successful(Right(apiDefinitionResult)))
    }

    def primeApiDefinitionSuccessWithRetiredApi(): ScalaOngoingStubbing[Future[Either[ApmConnector.FailedResult, ApmConnector.Result]]] = {
      val apiDefinitionResultRetired =
        ApmConnector.Result(Environment.PRODUCTION, getAccessTypeOfLatestVersion(apiDefinition1), serviceName, getLatestVersion(apiDefinition1), ApiStatus.RETIRED)

      when(mockApmConnector.fetchApiForServiceName(eqTo(serviceName))(*))
        .thenReturn(Future.successful(Right(apiDefinitionResultRetired)))
    }

    def primeApiDefinitionFailure(result: ApmConnector.FailedResult): ScalaOngoingStubbing[Future[Either[ApmConnector.FailedResult, ApmConnector.Result]]] = {
      when(mockApmConnector.fetchApiForServiceName(eqTo(serviceName))(*))
        .thenReturn(Future.successful(Left(result)))
    }

    def primeOasEnhanceSuccess(): ScalaOngoingStubbing[Either[ApiCataloguePublishResult, String]] = {
      when(mockOasParser.handleEnhancingOasForCatalogue(*[OasResult])).thenReturn(Right(expectedEnhancedOasString))
    }

    def primeOasEnhanceFailure(result: OpenApiEnhancementFailedResult): ScalaOngoingStubbing[Either[ApiCataloguePublishResult, String]] = {
      when(mockOasParser.handleEnhancingOasForCatalogue(*[OasResult])).thenReturn(Left(result))
    }

    def primeApiMicroserviceConnectorSuccess(): ScalaOngoingStubbing[Future[Either[Throwable, String]]] = {
      when(mockApmConnector.fetchApiDocumentationResource(*[Environment], *[ServiceName], *[ApiVersionNbr], *[String])).thenReturn(Future.successful(Right(yamlResponseString)))
    }

    def primeSuccessApartFromPublish(): ScalaOngoingStubbing[Either[ApiCataloguePublishResult, String]] = {
      primeApiDefinitionSuccess()
      primeApiMicroserviceConnectorSuccess()
      primeOasEnhanceSuccess()
    }

    def primePublishFail(): ScalaOngoingStubbing[Future[Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse]]] = {
      primeSuccessApartFromPublish()

      when(mockCatalogueConnector.publishApi(*[String])).thenReturn(Future.successful(Left(ApiCatalogueGeneralFailureResult("some error"))))
    }

    def primePublishSuccess(): ScalaOngoingStubbing[Future[Either[ApiCatalogueAdminConnector.ApiCatalogueFailedResult, PublishResponse]]] = {
      primeSuccessApartFromPublish()

      when(mockCatalogueConnector.publishApi(*[String])).thenReturn(Future.successful(Right(publishResponse)))
    }

  }

  "publishService" when {

    "getApiDefinitionByServiceName" should {
      "return Left ApiDefinitionInvalidStatusResult when connector returns definition with RETIRED status" in new Setup {
        val expectedError = ApiDefinitionInvalidStatusResult(serviceName, "definition record was RETIRED for this service")
        primeApiDefinitionSuccessWithRetiredApi()

        val result = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Left(error: ApiDefinitionInvalidStatusResult) => error shouldBe expectedError
          case _                                             => fail()
        }
      }

      "return Left when api definition get by service name fails, general fail" in new Setup {
        val expectedError = PublishFailedResult(serviceName, "some error")
        primeApiDefinitionFailure(ApmConnector.GeneralFailedResult("some error"))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Left(error: PublishFailedResult) => error shouldBe expectedError
          case _                                => fail()
        }

      }

      "return Left when api definition get by service name fails not found" in new Setup {
        val expectedError = ApiDefinitionNotFoundResult(serviceName, "some error")

        primeApiDefinitionFailure(ApmConnector.NotFoundResult("some error"))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Left(error: ApiDefinitionNotFoundResult) => error shouldBe expectedError
          case _                                        => fail()
        }

      }
    }

    "publishByServiceName Yaml flow" should {
      "return Right with publish details when publish is successful" in new Setup {
        primePublishSuccess()

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Right(value: PublishResponse) => value shouldBe publishResponse
          case _                             => fail()
        }
      }

      "return Left when publish to api catalogue fails" in new Setup {

        val expectedError = ApiCataloguePublishFailedResult(serviceName, "publish to catalogue failed some error")
        primePublishFail()

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Left(error: ApiCataloguePublishResult) => error shouldBe expectedError
          case _                                      => fail()
        }
      }

      "return Left when enhance OAS fails" in new Setup {
        val expectedError = OpenApiEnhancementFailedResult(serviceName, "some error")

        primeApiDefinitionSuccess()
        primeApiMicroserviceConnectorSuccess()
        primeOasEnhanceFailure(expectedError)

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))

        result match {
          case Left(error: OpenApiEnhancementFailedResult) => error shouldBe expectedError
          case _                                           => fail()
        }
      }
    }

  }

  "publishAll" should {

    "return left with error when connector returns an error" in new Setup {
      when(mockConnector.getAllServices()).thenReturn(Future.successful(Left(ApmConnector.GeneralFailedResult("error"))))
      val results = await(objInTest.publishAll())
      results match {
        case List(Right(_))                         => fail()
        case List(Left(error: PublishFailedResult)) => error shouldBe PublishFailedResult(ServiceName("All Services"), "something went wrong calling api definition")
      }

      verify(mockConnector).getAllServices()(*[HeaderCarrier])
    }

    "return results with Left when handleEnhancingOasForCatalogue fails" in new Setup {

      when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))
      primeApiMicroserviceConnectorSuccess()

      when(mockOasParser.handleEnhancingOasForCatalogue(*[OasResult])).thenReturn(Left(OpenApiEnhancementFailedResult(serviceName, "some error")))

      val results = await(objInTest.publishAll())
      results shouldBe List(
        Left(OpenApiEnhancementFailedResult(ServiceName("my-service"), "some error")),
        Left(OpenApiEnhancementFailedResult(ServiceName("my-service"), "some error"))
      )

      verify(mockConnector).getAllServices()(*[HeaderCarrier])

      verify(mockOasParser, times(2)).handleEnhancingOasForCatalogue(*[OasResult])
    }

    "return left when publish to catalogue fails" in new Setup {

      when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))
      primeApiMicroserviceConnectorSuccess()

      when(mockOasParser.handleEnhancingOasForCatalogue(*[OasResult])).thenReturn(Right("some valid oas yaml"))
      when(mockCatalogueConnector.publishApi(*[String])).thenReturn(Future.successful(Left(ApiCatalogueGeneralFailureResult("some error"))))

      val results = await(objInTest.publishAll())
      results shouldBe List(
        Left(ApiCataloguePublishFailedResult(ServiceName("my-service"), "publish to catalogue failed some error")),
        Left(ApiCataloguePublishFailedResult(ServiceName("my-service-2"), "publish to catalogue failed some error"))
      )

      verify(mockConnector).getAllServices()(*[HeaderCarrier])

      verify(mockOasParser, times(2)).handleEnhancingOasForCatalogue(*[OasResult])
      verify(mockCatalogueConnector, times(2)).publishApi(*[String])

    }

    "return right with publish responses when all successful" in new Setup {

      when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))
      primeApiMicroserviceConnectorSuccess()
      when(mockOasParser.handleEnhancingOasForCatalogue(*[OasResult])).thenReturn(Right("some valid oas yaml"))
      when(mockCatalogueConnector.publishApi(*[String])).thenReturn(Future.successful(Right(publishResponse)))

      val results = await(objInTest.publishAll())
      results shouldBe List(Right(publishResponse), Right(publishResponse))

      verify(mockConnector).getAllServices()(*[HeaderCarrier])

      verify(mockOasParser, times(2)).handleEnhancingOasForCatalogue(*[OasResult])
      verify(mockCatalogueConnector, times(2)).publishApi(*[String])
    }

  }
}

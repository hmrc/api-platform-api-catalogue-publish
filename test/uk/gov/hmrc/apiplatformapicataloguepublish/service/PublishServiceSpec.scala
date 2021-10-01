/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformapicataloguepublish.service

import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueGeneralFailureResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.{ ApiDefinitionGeneralFailedResult, ApiDefinitionResult}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{ApiAccess, PublicApiAccess}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{ConvertedWebApiToOasResult, GeneralOpenApiProcessingError}
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, OasParser}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import webapi.WebApiDocument

import java.util.{Optional, UUID}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PublishServiceSpec extends AnyWordSpec with MockitoSugar with Matchers with HeaderCarrierConverter
 with ApiDefinitionData with ScalaFutures with ApiDefinitionUtils with BeforeAndAfterEach {

  private val mockConnector = mock[ApiDefinitionConnector]
  private val mockApiRamlParser = mock[ApiRamlParser]
  private val mockOasParser = mock[OasParser]
  private val mockWebApiDocument = mock[WebApiDocument]
  private val mockCatalogueConnector = mock[ApiCatalogueAdminConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector, mockApiRamlParser, mockOasParser, mockWebApiDocument, mockCatalogueConnector)
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val apiDefinitionResult: ApiDefinitionResult = ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)
    val expectedDescription = "A description."
    val convertedWebApiToOasResult: ConvertedWebApiToOasResult = ConvertedWebApiToOasResult("", serviceName, expectedDescription)
  val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "someRef", API_PLATFORM)

    val objInTest = new PublishService(mockConnector, mockApiRamlParser, mockOasParser, mockCatalogueConnector)

    def primeBeforeCataloguePublish(apiDefinitionResult: ApiDefinitionResult,
                                    expectedDescription: String,
                                    convertedWebApiToOasResult: ConvertedWebApiToOasResult): Unit ={
      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDefinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess]))
        .thenReturn(Future.successful(convertedWebApiToOasResult))
      when(mockOasParser.enhanceOas(any[ConvertedWebApiToOasResult])).thenReturn(Right("oas string"))
    }

    def verifyMocksHappyPathBeforePublishCall() ={
      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
      verify(mockOasParser).enhanceOas(eqTo(convertedWebApiToOasResult))
    }

  
  }

  "publishByServiceName" should {

    "return Right with publish details when publish is successful" in new Setup {

      primeBeforeCataloguePublish(apiDefinitionResult, expectedDescription, convertedWebApiToOasResult)
 
      when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Right(publishResponse)))
      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Right(value: PublishResponse) => value shouldBe publishResponse
        case _            => fail()
      }

      verifyMocksHappyPathBeforePublishCall()

    }

    "return Left with error when publish is unsuccessful" in new Setup {

      primeBeforeCataloguePublish(apiDefinitionResult, expectedDescription, convertedWebApiToOasResult)

      when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Left(ApiCatalogueGeneralFailureResult("some error"))))
      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Left(x: ApiCataloguePublishFailedResult)            => succeed
        case _ => fail()
      }
      verifyMocksHappyPathBeforePublishCall()
    }

    "return a Left when ApiRamlParser returns an error" in new Setup {

      override val apiDefinitionResult: ApiDefinitionResult = ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)

      val errorMessage = "Parse failed"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDefinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.failed(new RuntimeException(errorMessage)))

      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Left(e: PublishFailedResult) => e.message shouldBe s"getRamlForApiDefinition failed: $errorMessage"
        case _            => fail()
      }

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verifyZeroInteractions(mockOasParser)
    }

    "return a Left when OasParser returns an error" in new Setup {

      val apiDeinitionResult: ApiDefinitionResult = ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)

      val errorMessage: String = "Parse failed"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDeinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess]))
        .thenReturn(Future.failed(new RuntimeException(errorMessage)))

      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Left(e: PublishFailedResult) => e.message shouldBe s"handleRamlToOas failed: $errorMessage"
        case _            => fail()
      }

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
    }


     "return Left with PublishFailedResult when connector returns Left with exception" in new Setup {

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Left(ApiDefinitionGeneralFailedResult("Some Message"))))

      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(PublishFailedResult("Some Message"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verifyZeroInteractions(mockApiRamlParser)
      verifyZeroInteractions(mockOasParser)

    }

    "return Left with OpenApiEnhancementFailedResult when oas parser returns Left with GeneralOpenApiProcessingError" in new Setup {

//     override val apiDefinitionResult: ApiDefinitionResult = ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)
//      override val expectedDescription = "A description."
//      override val convertedWebApiToOasResult: ConvertedWebApiToOasResult = ConvertedWebApiToOasResult("", serviceName, expectedDescription)
      val errorMessage = "Swagger Parse failure"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))).thenReturn(Future.successful(Right(apiDefinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess])).thenReturn(Future.successful(convertedWebApiToOasResult))
      when(mockOasParser.enhanceOas(any[ConvertedWebApiToOasResult])).thenReturn(Left(GeneralOpenApiProcessingError("apiName", errorMessage)))


      val result: Either[ApiCataloguePublishResult,PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(OpenApiEnhancementFailedResult(s"handleEnhancingOasForCatalogue failed: $errorMessage"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
      verify(mockOasParser).enhanceOas(eqTo(convertedWebApiToOasResult))

    }

    "return Left ApiDefinitionNotFoundResult when connector returns Left UpStream4xxResponse" in new Setup {

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Left(ApiDefinitionConnector.ApiDefinitionNotFoundResult("Some Message"))))

      val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(ApiDefinitionNotFoundResult("Some Message"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verifyZeroInteractions(mockApiRamlParser)
      verifyZeroInteractions(mockOasParser)

    }

  }
}

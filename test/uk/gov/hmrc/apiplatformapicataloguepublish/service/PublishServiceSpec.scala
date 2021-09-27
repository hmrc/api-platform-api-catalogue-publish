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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.ApiRamlParser
import webapi.WebApiDocument

import java.util.Optional
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.OasParser
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiAccess
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.ConvertedWebApiToOasResult
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.PublicApiAccess
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.GeneralOpenApiProcessingError

class PublishServiceSpec extends AnyWordSpec with MockitoSugar with Matchers with HeaderCarrierConverter
 with ApiDefinitionData with ScalaFutures with ApiDefinitionUtils with BeforeAndAfterEach {

  private val mockConnector = mock[ApiDefinitionConnector]
  private val mockApiRamlParser = mock[ApiRamlParser]
  private val mockOasParser = mock[OasParser]
  private val mockWebApiDocument = mock[WebApiDocument]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector, mockApiRamlParser, mockOasParser, mockWebApiDocument)
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val objInTest = new PublishService(mockConnector, mockApiRamlParser, mockOasParser)
  }

  "publishByServiceName" should {
    val serviceName = "service1"
    "return an Api Definition from connector when connector returns api definition" in new Setup {

      val apiDeinitionResult = ApiDefinitionConnector.ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)

      val expectedDescription = "A description."
      val convertedWebApiToOasResult = ConvertedWebApiToOasResult("", serviceName, expectedDescription)

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDeinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess]))
        .thenReturn(Future.successful(convertedWebApiToOasResult))
      when(mockOasParser.enhanceOas(any[ConvertedWebApiToOasResult])).thenReturn(Right("oas string"))  
      val result: Either[ParsedResult,String] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Right(value: String) => value shouldBe "oas string" 
        case _            => fail()
      }

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
      verify(mockOasParser).enhanceOas(eqTo(convertedWebApiToOasResult))

    }

    "return a Left when ApiRamlParser returns an error" in new Setup {

      val apiDeinitionResult = ApiDefinitionConnector.ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)

      val errorMessage = "Parse failed"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDeinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.failed(new RuntimeException(errorMessage)))

      val result: Either[ParsedResult,String] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Left(e: PublishFailedResult) => {e.message shouldBe s"getRamlForApiDefinition failed: $errorMessage"}
        case _            => fail()
      }

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verifyZeroInteractions(mockOasParser)
    }

    "return a Left when OasParser returns an error" in new Setup {

      val apiDeinitionResult: ApiDefinitionConnector.ApiDefinitionResult = ApiDefinitionConnector.ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)

      val errorMessage: String = "Parse failed"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDeinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess]))
        .thenReturn(Future.failed(new RuntimeException(errorMessage)))

      val result: Either[ParsedResult,String] = await(objInTest.publishByServiceName(serviceName))
      result match {
        case Left(e: PublishFailedResult) => {e.message shouldBe s"handleRamlToOas failed: $errorMessage"}
        case _            => fail()
      }

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
    }


     "return Left with PublishFailedResult when connector returns Left with exception" in new Setup {

      val exception = new RuntimeException(" unable to fetch definition")

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Left(exception)))

      val result: Either[ParsedResult,String] = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(PublishFailedResult("Unknown error occured"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verifyZeroInteractions(mockApiRamlParser)
      verifyZeroInteractions(mockOasParser)

    }

    "return Left with OpenApiEnhancementFailedResult when oas parser returns Left with GeneralOpenApiProcessingError" in new Setup {

      val apiDeinitionResult = ApiDefinitionConnector.ApiDefinitionResult(getRamlUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName)
      val expectedDescription = "A description."
      val convertedWebApiToOasResult = ConvertedWebApiToOasResult("", serviceName, expectedDescription)
      val errorMessage = "Swagger Parse failure"

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))).thenReturn(Future.successful(Right(apiDeinitionResult)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))
      when(mockOasParser.parseWebApiDocument(any[WebApiDocument], any[String], any[ApiAccess])).thenReturn(Future.successful(convertedWebApiToOasResult))
      when(mockOasParser.enhanceOas(any[ConvertedWebApiToOasResult])).thenReturn(Left(GeneralOpenApiProcessingError("apiName", errorMessage)))


      val result: Either[ParsedResult,String] = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(OpenApiEnhancementFailedResult(s"handleEnhancingOasForCatalogue failed: $errorMessage"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(getRamlUri(apiDefinition1)))
      verify(mockOasParser).parseWebApiDocument(eqTo(mockWebApiDocument), eqTo(serviceName), eqTo(PublicApiAccess()))
      verify(mockOasParser).enhanceOas(eqTo(convertedWebApiToOasResult))

    }

    "return Left ApiDefinitionNotFoundResult when connector returns Left UpStream4xxResponse" in new Setup {

      val notFoundException = new Upstream4xxResponse("unable to fetch definition", 404, 404)

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Left(notFoundException)))

      val result = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(ApiDefinitionNotFoundResult("unable to fetch definition"))

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verifyZeroInteractions(mockApiRamlParser)
      verifyZeroInteractions(mockOasParser)

    }

  }
}

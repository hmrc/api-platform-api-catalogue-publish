/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueGeneralFailureResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.{ApiCatalogueAdminConnector, ApiMicroserviceConnector}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.{ApiDefinitionResult, GeneralFailedResult}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{ApiAccess, ApiDefinition, ApiStatus, PublicApiAccess}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{GeneralOpenApiProcessingError, OasResult}
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, OasParser}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import webapi.WebApiDocument

import java.util.{Optional, UUID}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PublishServiceSpec
    extends AnyWordSpec
    with MockitoSugar
    with Matchers
    with HeaderCarrierConverter
    with ApiDefinitionData
    with ScalaFutures
    with ApiDefinitionUtils {

  trait Setup {
    val mockConnector = mock[ApiDefinitionConnector]
    val mockOasParser = mock[OasParser]
    val mockWebApiDocument = mock[WebApiDocument]
    val mockRaml2OasService = mock[Raml2OasService]
    val mockCatalogueConnector = mock[ApiCatalogueAdminConnector]
    val mockApiMicroserviceConnector = mock[ApiMicroserviceConnector]
    import java.nio.file.{Files, Paths}

    val filePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "test/resources/noIntCatExtensions.yaml"
    val expectedOasFilePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "test/resources/expectedYamlConverted.yaml"

    def getRamlUri(definition: ApiDefinition) = {
      getUri(definition) + ".raml"
    }
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val yamlResponseString = Files.readAllLines(Paths.get(filePath)).asScala.mkString
    val expectedEnhancedOasString = Files.readAllLines(Paths.get(expectedOasFilePath)).asScala.mkString
    val apiDefinitionResult: ApiDefinitionResult = ApiDefinitionResult(getUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName, ApiStatus.STABLE)

    val apiDefinitionResult2: ApiDefinitionResult =
      ApiDefinitionResult(getUri(apiDefinition2), getAccessTypeOfLatestVersion(apiDefinition2), apiDefinition2.serviceName, ApiStatus.STABLE)
    val expectedDescription = "A description."
    val convertedWebApiToOasResult: OasResult = OasResult("", serviceName, expectedDescription)
    val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "someRef", API_PLATFORM)

    val objInTest = new PublishService(mockConnector, mockRaml2OasService, mockOasParser, mockCatalogueConnector, mockApiMicroserviceConnector)
    val publicAccessAsString = "Public Access"

    def primeBeforeCataloguePublish(apiDefinitionResult: ApiDefinitionResult, expectedDescription: String, convertedWebApiToOasResult: OasResult): Unit = {
      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDefinitionResult)))

      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))

      when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Right("oas string"))
    }

    def verifyMocksHappyPathBeforePublishCall() = {
      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

    }

  }
  "publishService" when {
    "publishByServiceName" should {

      "return Right with publish details when publish is successful for raml" in new Setup {
        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Left(new NotFoundException("error"))))

        primeBeforeCataloguePublish(apiDefinitionResult, expectedDescription, convertedWebApiToOasResult)

        when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Right(publishResponse)))
        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result match {
          case Right(value: PublishResponse) => value shouldBe publishResponse
          case _                             => fail()
        }



        verifyMocksHappyPathBeforePublishCall()

      }

      "return Right with publish details when publish is successful for yaml" in new Setup {
        primeBeforeCataloguePublish(apiDefinitionResult, expectedDescription, convertedWebApiToOasResult)

        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Right(yamlResponseString)))
        when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Right(publishResponse)))
        when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Right(expectedEnhancedOasString))


        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result match {
          case Right(value: PublishResponse) => value shouldBe publishResponse
          case _                             => fail()
        }


        verifyMocksHappyPathBeforePublishCall()
        verify(mockCatalogueConnector).publishApi(eqTo(expectedEnhancedOasString))
        verify(mockOasParser).enhanceOas(eqTo(OasResult(yamlResponseString, apiDefinitionResult.serviceName, publicAccessAsString)))
      }

      "return Left with error when publish is unsuccessful" in new Setup {
        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Left(new NotFoundException("error"))))
        primeBeforeCataloguePublish(apiDefinitionResult, expectedDescription, convertedWebApiToOasResult)

        when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Left(ApiCatalogueGeneralFailureResult("some error"))))
        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result match {
          case Left(x: ApiCataloguePublishFailedResult) => succeed
          case _                                        => fail()
        }
        verifyMocksHappyPathBeforePublishCall()
      }

      "return a Left when ApiRamlParser returns an error" in new Setup {
        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Left(new NotFoundException("error"))))

        override val apiDefinitionResult: ApiDefinitionResult =
          ApiDefinitionResult(getUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName, ApiStatus.STABLE)

        val errorMessage = "Parse failed"

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
          .thenReturn(Future.successful(Right(apiDefinitionResult)))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result match {
          case Left(e: PublishFailedResult) => e.message shouldBe s"getRamlForApiDefinition failed: $errorMessage"
          case _                            => fail()
        }

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

        verifyZeroInteractions(mockOasParser)
      }

      "return a Left when OasParser returns an error" in new Setup {
        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Left(new NotFoundException("error"))))

        val apiDeinitionResult: ApiDefinitionResult = ApiDefinitionResult(getUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName, ApiStatus.STABLE)

        val errorMessage: String = "Parse failed"

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
          .thenReturn(Future.successful(Right(apiDeinitionResult)))


        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result match {
          case Left(e: PublishFailedResult) => e.message shouldBe s"handleRamlToOas failed: $errorMessage"
          case _                            => fail()
        }

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

      }

      "return Left with PublishFailedResult when connector returns Left with exception" in new Setup {
        when(mockApiMicroserviceConnector.fetchApiDocumentationResourceByUrl(any[String])).thenReturn(Future.successful(Left(new NotFoundException("error"))))

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
          .thenReturn(Future.successful(Left(GeneralFailedResult("Some Message"))))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result shouldBe Left(PublishFailedResult(serviceName, "Some Message"))

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

        verifyZeroInteractions(mockOasParser)

      }

      "return Left with OpenApiEnhancementFailedResult when oas parser returns Left with GeneralOpenApiProcessingError" in new Setup {
        val errorMessage = "Swagger Parse failure"

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))).thenReturn(Future.successful(Right(apiDefinitionResult)))

        when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))

        when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Left(GeneralOpenApiProcessingError(serviceName, errorMessage)))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result shouldBe Left(OpenApiEnhancementFailedResult(serviceName, s"handleEnhancingOasForCatalogue failed: $errorMessage"))

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

        verify(mockOasParser).enhanceOas(eqTo(convertedWebApiToOasResult))

      }

      "return Left ApiDefinitionNotFoundResult when connector returns Left UpStream4xxResponse" in new Setup {

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
          .thenReturn(Future.successful(Left(ApiDefinitionConnector.NotFoundResult("Some Message"))))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result shouldBe Left(ApiDefinitionNotFoundResult(serviceName, "Some Message"))

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

        verifyZeroInteractions(mockOasParser)

      }

      "return Left ApiDefinitionInvalidStatusResult when connector returns definition with RETIRED status" in new Setup {
        val apiDeinitionResult: ApiDefinitionResult = ApiDefinitionResult(getUri(apiDefinition1), getAccessTypeOfLatestVersion(apiDefinition1), serviceName, ApiStatus.RETIRED)

        when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
          .thenReturn(Future.successful(Right(apiDeinitionResult)))

        val result: Either[ApiCataloguePublishResult, PublishResponse] = await(objInTest.publishByServiceName(serviceName))
        result shouldBe Left(ApiDefinitionInvalidStatusResult(serviceName, "definition record was RETIRED for this service"))

        verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

        verifyZeroInteractions(mockOasParser)

      }

    }

    "publishAll" should {

      "return left with error when connector returns an error" in new Setup {
        when(mockConnector.getAllServices()).thenReturn(Future.successful(Left(GeneralFailedResult("error"))))
        val results = await(objInTest.publishAll())
        results match {
          case List(Right(publishResponse))           => fail
          case List(Left(error: PublishFailedResult)) => error shouldBe PublishFailedResult("All Services", "something went wrong calling api definition")
        }

        verify(mockConnector).getAllServices()(any[HeaderCarrier])
      }

      "return left with error when we are unable to obtain raml from service" in new Setup {
        when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))

        val results = await(objInTest.publishAll())
        results shouldBe List(
          Left(PublishFailedResult("my-service", "getRamlForApiDefinition failed: errorMessage")),
          Left(PublishFailedResult("my-service-2", "getRamlForApiDefinition failed: errorMessage"))
        )

        verify(mockConnector).getAllServices()(any[HeaderCarrier])

      }

      "return left when raml to oas parse fails" in new Setup {
        when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))

        val results = await(objInTest.publishAll())
        results shouldBe List(
          Left(PublishFailedResult("my-service", "handleRamlToOas failed: errorMessage")),
          Left(PublishFailedResult("my-service-2", "handleRamlToOas failed: errorMessage"))
        )

        verify(mockConnector).getAllServices()(any[HeaderCarrier])

      }

      "return results with Left when oas extensions fail" in new Setup {

        when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))

        when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Left(GeneralOpenApiProcessingError(serviceName, "some error")))

        val results = await(objInTest.publishAll())
        results shouldBe List(
          Left(OpenApiEnhancementFailedResult("my-service", "handleEnhancingOasForCatalogue failed: some error")),
          Left(OpenApiEnhancementFailedResult("my-service", "handleEnhancingOasForCatalogue failed: some error"))
        )

        verify(mockConnector).getAllServices()(any[HeaderCarrier])


        verify(mockOasParser, times(2)).enhanceOas(any[OasResult])
      }

      "return left when publish to catalogue fails" in new Setup {

        when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))

        when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Right("some valid oas yaml"))
        when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Left(ApiCatalogueGeneralFailureResult("some error"))))

        val results = await(objInTest.publishAll())
        results shouldBe List(
          Left(ApiCataloguePublishFailedResult("my-service", "publish to catalogue failed some error")),
          Left(ApiCataloguePublishFailedResult("my-service-2", "publish to catalogue failed some error"))
        )

        verify(mockConnector).getAllServices()(any[HeaderCarrier])


        verify(mockOasParser, times(2)).enhanceOas(any[OasResult])
        verify(mockCatalogueConnector, times(2)).publishApi(any[String])

      }

      "return right with publish responses when all successful" in new Setup {

        when(mockConnector.getAllServices()).thenReturn(Future.successful(Right(List(apiDefinitionResult, apiDefinitionResult2))))
        when(mockOasParser.enhanceOas(any[OasResult])).thenReturn(Right("some valid oas yaml"))
        when(mockCatalogueConnector.publishApi(any[String])).thenReturn(Future.successful(Right(publishResponse)))

        val results = await(objInTest.publishAll())
        results shouldBe List(Right(publishResponse), Right(publishResponse))

        verify(mockConnector).getAllServices()(any[HeaderCarrier])

        verify(mockOasParser, times(2)).enhanceOas(any[OasResult])
        verify(mockCatalogueConnector, times(2)).publishApi(any[String])
      }

    }
  }
}

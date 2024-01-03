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

package uk.gov.hmrc.apicataloguepublish.parser

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.BeforeAndAfterEach

import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ServiceName
import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apicataloguepublish.openapi.{GeneralOpenApiProcessingError, OasResult, OpenApiProcessingError}
import uk.gov.hmrc.apicataloguepublish.service.{ApiCataloguePublishResult, OpenApiEnhancementFailedResult}

class OasParserSpec extends HmrcSpec with OasStringUtils with BeforeAndAfterEach {

  val mockDateTimeWrapper = mock[DateTimeWrapper]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeWrapper)
  }

  trait Setup {
    val objInTest = new OasParser(mockDateTimeWrapper)
  }

  "handleEnhancingOasForCatalogue" should {
    "return right with enhanced OAS when successful" in new Setup {
      val convertedOasResult: OasResult = OasResult(oasStringWithDescription, ServiceName("apiName"), "PRIVATE")
      val validISODate: String          = "2021-12-25T12:00:00Z"
      when(mockDateTimeWrapper.generateDateNowString()).thenReturn(validISODate)

      val result: Either[ApiCataloguePublishResult, String] = objInTest.handleEnhancingOasForCatalogue(convertedOasResult)

      result match {
        case Right(convertedOas: String) => convertedOas shouldBe oasStringWithEnhancements
        case Left(_)                     => fail()
      }
    }

    "return Left when oas is invalid" in new Setup {
      val convertedOasResult: OasResult = OasResult("something invalid", ServiceName("apiName"), "PRIVATE")

      val result: Either[ApiCataloguePublishResult, String] = objInTest.handleEnhancingOasForCatalogue(convertedOasResult)

      result match {
        case Right(_)                                => fail()
        case Left(e: OpenApiEnhancementFailedResult) => e.message shouldBe "handleEnhancingOasForCatalogue failed: Swagger Parse failure"
      }
    }

  }

  "enhanceOas" should {
    "return right with enhanced OAS when successful" in new Setup {

      val convertedOasResult: OasResult = OasResult(oasStringWithDescription, ServiceName("apiName"), "PRIVATE")
      val validISODate: String          = "2021-12-25T12:00:00Z"
      when(mockDateTimeWrapper.generateDateNowString()).thenReturn(validISODate)

      val result: Either[OpenApiProcessingError, String] = objInTest.enhanceOas(convertedOasResult)

      result match {
        case Right(convertedOas: String) => convertedOas shouldBe oasStringWithEnhancements
        case Left(_)                     => fail()
      }
    }

    "return Left when oas is invalid" in new Setup {
      val convertedOasResult: OasResult = OasResult("something invalid", ServiceName("apiName"), "PRIVATE")

      val result: Either[OpenApiProcessingError, String] = objInTest.enhanceOas(convertedOasResult)

      result match {
        case Right(_)                               => fail()
        case Left(e: GeneralOpenApiProcessingError) => e.message shouldBe "Swagger Parse failure"
      }
    }
  }
}

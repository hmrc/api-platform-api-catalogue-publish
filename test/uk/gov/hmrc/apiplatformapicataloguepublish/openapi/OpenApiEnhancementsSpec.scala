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

package uk.gov.hmrc.apiplatformapicataloguepublish.openapi

import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{ConvertedWebApiToOasResult, GeneralOpenApiProcessingError}

import scala.io.Source

class OpenApiEnhancementsSpec extends AnyWordSpec with Matchers with OpenApiEnhancements {

  trait Setup {

    val validISODate = "2021-12-25T12:00:00Z"

    def getFileContents(filePath: String): String = {
      Source.fromResource(filePath).mkString
    }

    def getInfo(yaml: String) = {
      val options: ParseOptions = new ParseOptions()
      options.setResolve(false)
      Option(new OpenAPIV3Parser().readContents(yaml, null, options))
        .flatMap(swaggerResult => Option(swaggerResult.getOpenAPI()))
        .flatMap(openapi => Option(openapi.getInfo()))
    }

    def getShortDescriptionFromOasString(yaml: String): Option[String] = {
      getInfo(yaml)
        .flatMap(info => Option(info.getExtensions()))
        .flatMap(extensions => Option(extensions.get(EXTENSIONS_KEY).asInstanceOf[java.util.Map[String, Any]]))
        .map(subLevelExtensions => subLevelExtensions.get(SHORT_DESC_EXTENSION_KEY).toString)
    }

    def getDescriptionFromOasString(yaml: String): Option[String] = {
      getInfo(yaml)
        .flatMap(info => Option(info.getDescription()))
    }

    def validateExpectedFileContents(inputFile: String, expectedFile: String) = {
      val contentsToParse = getFileContents(inputFile)
      val expectedYaml = getFileContents(expectedFile)
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."), validISODate) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          println(s"****** parsedString is: $oasString")
          oasString shouldBe expectedYaml
        }
      }
    }

  }

  "addOasSpecAttributes " should {
    "handle empty string" in new Setup {
      addOasSpecAttributes(ConvertedWebApiToOasResult("", "iamAnApi", "This is a public API."), validISODate) shouldBe Left(GeneralOpenApiProcessingError("iamAnApi", "Swagger Parse failure"))

    }

    "add integration catalogue extensions to expected yaml file when short-description DOESN'T need to be truncated" in new Setup {
      validateExpectedFileContents("noIntCatExtensions.yaml", "expectedWithIntCatExtensions.yaml")
    }

    "Return Left(GeneralOpenApiProcessingError) if contents to parse has invalid x-amf-userDocumentation content" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-withInvalidXAmfDocumentation.yaml")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."), validISODate) match {
        case Right(_)                                   => fail()
        case Left(error: GeneralOpenApiProcessingError) => {
          error.apiName shouldBe "iamAnApi"
          error.message shouldBe "API: iamAnApi content for title: Versioning points to a file!! https://developer.service.hmrc.gov.uk/api-documentation/assets/common/docs/versioning.md \nAPI: iamAnApi content for title: Errors points to a file!! https://developer.service.hmrc.gov.uk/api-documentation/assets/common/docs/errors.md \n"
        }
      }

    }
    "Return Right if contents to parse does not have x-amf-userDocumentation tag" in new Setup {
      validateExpectedFileContents("noIntCatExtensions-withNOxAmfDocumentation.yaml", "expectedWithIntCatExtensions-NOxAmfDocumentation.yaml")
    }

    "add integration catalogue extensions to expected yaml file when short-description DOES need to be truncated" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-with-long-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-truncated-short-decription.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."), validISODate) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")

          resultingShortDesc shouldBe expectedShortDesc
        }
      }
    }

    "add accessType description to expected yaml file when description does not exist" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-no-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."), validISODate) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")
          val resultingDescription = getDescriptionFromOasString(oasString).getOrElse("")

          resultingShortDesc shouldBe expectedShortDesc
          resultingDescription shouldBe expectedDescription
        }
      }

    }

    "add accessType description to expected yaml file when description is empty" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-empty-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."), validISODate) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")
          val resultingDescription = getDescriptionFromOasString(oasString).getOrElse("")

          resultingDescription shouldBe expectedDescription
          resultingShortDesc shouldBe expectedShortDesc
        }
      }
    }

    "Return Right if contents to parse contains path AND operation level Content-Type headers" in new Setup {
      validateExpectedFileContents("with-path-and-op-level-ContentType-Header.yaml", "expected-with-path-and-op-level-ContentType-Header.yaml")
    }

    "Return Right if contents to parse ONLY contains path level Content-Type header" in new Setup {
      validateExpectedFileContents("with-only-path-level-ContentType-Header.yaml", "expected-with-only-path-level-ContentType-Header.yaml")
    }

    "Return Right if contents to parse ONLY contains operation level Content-Type header" in new Setup {
      validateExpectedFileContents("with-only-op-level-ContentType-Header.yaml", "expected-with-only-op-level-ContentType-Header.yaml")
    }

    "Return Right if contents to parse contains path AND operation level Accept headers" in new Setup {
      validateExpectedFileContents("with-path-and-op-level-Accept-Header.yaml", "expected-with-path-and-op-level-Accept-Header.yaml")
    }

    "Return Right if contents to parse ONLY contains path level Accept header" in new Setup {
      validateExpectedFileContents("with-only-path-level-Accept-Header.yaml", "expected-with-only-path-level-Accept-Header.yaml")
    }

    "Return Right if contents to parse ONLY contains operation level Accept header" in new Setup {
      validateExpectedFileContents("with-only-op-level-Accept-Header.yaml", "expected-with-only-op-level-Accept-Header.yaml")
    }

    "Return Right if contents to parse does not contain any Accept or Content-Type headers" in new Setup {
      validateExpectedFileContents("no-Accept-or-ContentType-Headers.yaml", "expected-no-ContentType-or-Accept-Headers.yaml")
    }

    "Return Right if contents to parse contains Security tag with OAuth" in new Setup {
      validateExpectedFileContents("with-security-oauth-tag.yaml", "expected-with-op-level-Authorization-Header.yaml")
    }

    "Return Right with examples when input file contains x-amf-examples in the Request and Response" in new Setup {
      validateExpectedFileContents("with-x-amf-examples-in-request-and-response.yaml", "expected-with-examples-in-request-and-response.yaml")
    }      
    
    "Return Right with examples when input file contains x-amf-examples containing description and value" in new Setup {
    validateExpectedFileContents("with-x-amf-examples-containing-description-and-value.yaml", "expected-with-examples-containing-description-and-value.yaml")
    }
  }
}

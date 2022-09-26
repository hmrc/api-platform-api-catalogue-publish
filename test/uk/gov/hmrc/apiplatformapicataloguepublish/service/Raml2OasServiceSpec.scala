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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{PrivateApiAccess, PublicApiAccess}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.OasResult
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, Oas30Wrapper, OasStringUtils}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import webapi.{Raml10, WebApiDocument}

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class Raml2OasServiceSpec
  extends AnyWordSpec
    with MockitoSugar
    with Matchers
    with HeaderCarrierConverter
    with ApiDefinitionData
    with ScalaFutures
    with ApiDefinitionUtils
    with OasStringUtils
    with DefaultAwaitTimeout {

  trait Setup {
    val mockOas30Wrapper = mock[Oas30Wrapper]
  val mockApiRamlParser = mock[ApiRamlParser]

    val objInTest = new Raml2OasService(mockOas30Wrapper, mockApiRamlParser)

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def webApiDocumentWithDescription = getWebApiDocument("test-ramlFile-with-description.raml")
  }


  "parseWebApiDocument" should {

    val serviceName = "service1"
    val publicAccessTypeDescription = "This is a public API."
    val privateAccessTypeDescription = "This is a private API."

    "return a ConvertedWebApiToOasResult when API is Public" in new Setup {

      when(mockOas30Wrapper.ramlToOas(any[WebApiDocument]))
        .thenReturn(Future.successful(oasStringWithDescription))
      val result: OasResult = await(objInTest.parseWebApiDocument(webApiDocumentWithDescription, serviceName, PublicApiAccess()))

      result.oasAsString shouldBe oasStringWithDescription
      result.apiName shouldBe serviceName
      result.accessTypeDescription shouldBe publicAccessTypeDescription

      verify(mockOas30Wrapper).ramlToOas(any[WebApiDocument])
    }

    "return a Right(ConvertedWebApiToOasResult) when API is Private" in new Setup {

      when(mockOas30Wrapper.ramlToOas(any[WebApiDocument])).thenReturn(Future.successful(oasStringWithDescription))
      val result: OasResult = await(objInTest.parseWebApiDocument(webApiDocumentWithDescription, serviceName, PrivateApiAccess()))

      result.oasAsString shouldBe oasStringWithDescription
      result.apiName shouldBe serviceName
      result.accessTypeDescription shouldBe privateAccessTypeDescription

      verify(mockOas30Wrapper).ramlToOas(any[WebApiDocument])
    }

  }

}

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

package uk.gov.hmrc.apiplatformapicataloguepublish.parser

import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import webapi.{WebApiDocument, Raml10}

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.PublicApiAccess
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.ConvertedWebApiToOasResult
import amf.core.model.document.Document
import amf.core.parser.{Annotations, Fields}
import amf.core.model.domain.Annotation

import java.util.concurrent.TimeUnit
import scala.io.Source

class OasParserSpec extends AnyWordSpec with MockitoSugar with Matchers with HeaderCarrierConverter with OasStringUtils with ScalaFutures {

  // private val mockWebApiDocument = mock[WebApiDocument]
  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val objInTest = new OasParser

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def webApiDocumentWithDescription = getWebApiDocument("test-ramlFile-with-description.raml")
  }

  "parseWebApiDocument" should {
    val serviceName = "service1"
    val convertedWebApiToOasResult = ConvertedWebApiToOasResult("", "customs-declarations", "This is a public API.")
    "return a ConvertedWebApiToOasResult" in new Setup {
      val result = await(objInTest.parseWebApiDocument(serviceName, Some(PublicApiAccess()), webApiDocumentWithDescription))
      result match {
        case Right(value) => value shouldBe oasWithDescription
        case _            => fail()
      }

    }

  }
}

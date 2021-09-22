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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.service

import org.mockito.ArgumentMatchersSugar.{eqTo, any}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiRamlParser
import uk.gov.hmrc.http.NotFoundException
import webapi.WebApiDocument
import java.util.Optional
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinition


class PublishServiceSpec extends AnyWordSpec with MockitoSugar  with Matchers
  with HeaderCarrierConverter with ApiDefinitionData with ScalaFutures{

  private val mockConnector = mock[ApiDefinitionConnector]
  private val mockApiRamlParser = mock[ApiRamlParser]
  private val mockWebApiDocument = mock[WebApiDocument]
  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val objInTest = new PublishService(mockConnector, mockApiRamlParser)
  }


  "publishByServiceName" should {
    val serviceName = "service1"
    "return an Api Definition from connector when connector returns api definition" in new Setup{


      val expectedDescription = "A description."
      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Right(apiDefinition1)))
      when(mockApiRamlParser.getRaml(any[String])).thenReturn(Future.successful(mockWebApiDocument))
      when(mockWebApiDocument.raw).thenReturn(Optional.of(expectedDescription))
      
      val result = await(objInTest.publishByServiceName(serviceName))
      result match {
       case Right(value) => value shouldBe expectedDescription
       case _ => fail()
      }


      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))
      verify(mockApiRamlParser).getRaml(eqTo(ApiDefinition.getRamlUri(apiDefinition1)))

    }

    "return None when connector returns None" in new Setup{

      val notFoundException = new NotFoundException(" unable to fetch definition")

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Left(notFoundException)))
      val result  = await(objInTest.publishByServiceName(serviceName))
      result shouldBe Left(notFoundException)

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

    }

  }
}

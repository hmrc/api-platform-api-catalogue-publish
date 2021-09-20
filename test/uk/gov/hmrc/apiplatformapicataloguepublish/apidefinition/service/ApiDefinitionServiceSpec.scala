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

import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinition
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ApiDefinitionServiceSpec extends AnyWordSpec with MockitoSugar  with Matchers
  with HeaderCarrierConverter with ApiDefinitionData with ScalaFutures{

  private val mockConnector = mock[ApiDefinitionConnector]

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val objInTest = new ApiDefinitionService(mockConnector)
  }

  "getDefinitionByServiceName" should {
    val serviceName = "service1"
    "return an Api Definition from connector when connector returns api definition" in new Setup{

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(Option(apiDefinition1)))
      val result: Option[String] = await(objInTest.getDefinitionByServiceName(serviceName))
      result shouldBe Some("http://localhost:9820/api/conf/1.0/application.raml")

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

    }

    "return None when connector returns None" in new Setup{

      when(mockConnector.getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc)))
        .thenReturn(Future.successful(None))
      val result: Option[String] = await(objInTest.getDefinitionByServiceName(serviceName))
      result shouldBe None

      verify(mockConnector).getDefinitionByServiceName(eqTo(serviceName))(eqTo(hc))

    }

  }
}

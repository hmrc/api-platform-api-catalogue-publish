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

package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import org.scalatest.matchers.should.Matchers
import org.mockito.ArgumentMatchersSugar.{eqTo, any}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import org.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.service.ApiDefinitionService
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier


class PublishControllerSpec extends AnyWordSpec with MockitoSugar with  Matchers with ApiDefinitionData {

  private val fakeRequest = FakeRequest("POST", "/")
  private val mockApiDefinitionService = mock[ApiDefinitionService]
  private val controller = new PublishController(mockApiDefinitionService, Helpers.stubControllerComponents())


  "POST /publish/[service-name]" should {
   val serviceName = "service1"
    "return 200 and an api defintion" in {
      when(mockApiDefinitionService.getDefinitionByServiceName(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Option(apiDefinition1)))
      val result = controller.publish(serviceName)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe "Hello, defintion for service1 found with 2 versions"
      verify(mockApiDefinitionService).getDefinitionByServiceName(eqTo(serviceName))(any[HeaderCarrier])
    }

    "return 200 and NO api defintion" in {
      when(mockApiDefinitionService.getDefinitionByServiceName(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(None))
      val result = controller.publish(serviceName)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe "Hello, no definition found for service1"
      verify(mockApiDefinitionService).getDefinitionByServiceName(eqTo(serviceName))(any[HeaderCarrier])
    }
  }
}

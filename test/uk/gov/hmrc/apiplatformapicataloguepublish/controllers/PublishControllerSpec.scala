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


import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.service.{ApiDefinitionNotFoundResult, PublishFailedResult, PublishService}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PublishControllerSpec extends AnyWordSpec with MockitoSugar with BeforeAndAfterEach
  with Matchers with ApiDefinitionData with ApiCatalogueAdminJsonFormatters{

  private val fakeRequest = FakeRequest("POST", "/")
  private val mockPublishService = mock[PublishService]
  private val controller = new PublishController(mockPublishService, Helpers.stubControllerComponents())

    override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockPublishService)
  }


  "POST /publish/[service-name]" should {
   val serviceName = "service1"
    "return 200 and an api definition" in {

      val publishResult = PublishResponse(IntegrationId(UUID.randomUUID()), "someRef", API_PLATFORM)
      when(mockPublishService.publishByServiceName(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(publishResult)))
      val result: Future[Result] = controller.publish(serviceName)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe Json.toJson(publishResult).toString()
      verify(mockPublishService).publishByServiceName(eqTo(serviceName))(any[HeaderCarrier])
    }

    "return 200 and NO api definition" in {
      when(mockPublishService.publishByServiceName(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Left(PublishFailedResult("",""))))
      
      val result: Future[Result] = controller.publish(serviceName)(fakeRequest)
     
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      verify(mockPublishService).publishByServiceName(eqTo(serviceName))(any[HeaderCarrier])
    }

    "return 404 and NO api definition" in {
      when(mockPublishService.publishByServiceName(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Left(ApiDefinitionNotFoundResult("",""))))
      
      val result: Future[Result] = controller.publish(serviceName)(fakeRequest)
      
      status(result) shouldBe Status.NOT_FOUND
      verify(mockPublishService).publishByServiceName(eqTo(serviceName))(any[HeaderCarrier])
    }
  }
}

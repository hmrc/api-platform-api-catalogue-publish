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

import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.MockitoSugar


class ApiDefinitionServiceSpec extends AnyWordSpec with MockitoSugar with Matchers {

  private val connector = mock[ApiDefinitionConnector]
  private val service = new ApiDefinitionService(connector)

  trait Setup {
  }

  "getDefinitionByServiceName" should {
    "return an Api Definition" in {
      val serviceName = "service1"

    }
  }
}

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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils

import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.PublicApiAccess

class ApiDefinitionUtilsSpec extends AnyWordSpec with ApiDefinitionData with ApiDefinitionUtils with Matchers {

  "getRamlUri" should {
    "return correct uri" in {
        getUri(apiDefinition1)  shouldBe "serviceBaseUrl/api/conf/2.0/application"
    }
  }

  "getLatestVersion" should {
    "return latest version" in {
      getLatestVersion(apiDefinition1) shouldBe "2.0"
    }

    "return 1.0 when definition has no versions" in {
      getLatestVersion(apiDefinition1.copy(versions = List.empty)) shouldBe "1.0"
    }
  }

  "getAccessTypeOfLatestVersion" should {
    "return access type of latest version" in {
      getAccessTypeOfLatestVersion(apiDefinition1) shouldBe PublicApiAccess()
    }

     "return public when definition has no versions" in {
      getAccessTypeOfLatestVersion(apiDefinition1.copy(versions = List.empty)) shouldBe PublicApiAccess()
    }
  }
}
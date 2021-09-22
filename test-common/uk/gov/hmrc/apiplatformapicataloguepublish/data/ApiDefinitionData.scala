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

package uk.gov.hmrc.apiplatformapicataloguepublish.data

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionBuilder


trait ApiDefinitionData extends ApiDefinitionBuilder {
      val categories = List(ApiCategory("category1"), ApiCategory("category2"))
    val serviceName = "my-service"
    val versions = List(apiVersion(version = ApiVersion("1.0")), apiVersion(version = (ApiVersion("2.0"))))

    val apiDefinition1 = ApiDefinition("serviceBaseUrl", serviceName, s"$serviceName-name", s"$serviceName-description", ApiContext(s"$serviceName-context"), false, false, versions.toList, categories)
}

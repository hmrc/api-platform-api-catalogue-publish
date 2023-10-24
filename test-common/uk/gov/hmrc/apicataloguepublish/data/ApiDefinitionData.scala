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

package uk.gov.hmrc.apicataloguepublish.data

import uk.gov.hmrc.apiplatform.modules.apis.domain.models._
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionBuilder

trait ApiDefinitionData extends ApiDefinitionBuilder {
  val categories  = List(ApiCategory.AGENTS, ApiCategory.BUSINESS_RATES)
  val serviceName = ServiceName("my-service")
  val versions    = List(apiVersion(version = ApiVersionNbr("1.0")), apiVersion(version = (ApiVersionNbr("2.0"))))

  val apiDefinition1 = ApiDefinition(
    serviceName,
    "serviceBaseUrl",
    s"$serviceName-name",
    s"$serviceName-description",
    ApiContext(s"$serviceName-context"),
    versions = ApiVersions.fromList(versions.toList),
    false,
    false,
    None,
    categories
  )

  val apiDefinition2 = ApiDefinition(
    ServiceName(s"$serviceName-2"),
    "serviceBaseUrl2",
    s"$serviceName-name2",
    s"$serviceName-description2",
    ApiContext(s"$serviceName-context2"),
    ApiVersions.fromList(versions.toList),
    false,
    false,
    None,
    categories
  )
}

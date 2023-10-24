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

package uk.gov.hmrc.apicataloguepublish.apidefinition.utils

import uk.gov.hmrc.apiplatform.modules.apis.domain.models._
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApiVersionNbr

trait ApiDefinitionUtils {

  def getUri(apiDefinition: ApiDefinition) = {
    getBaseUrl(apiDefinition) + s"/api/conf/${getLatestVersion(apiDefinition)}/application"
  }

  private def getBaseUrl(apiDefinition: ApiDefinition): String = {
    apiDefinition.serviceBaseUrl
  }

  private val sortedVersionsHighestFirst: ApiDefinition => List[ApiVersion] = (defn) =>
    defn.versionsAsList
      .sorted
      .reverse

  def getLatestVersion(apiDefinition: ApiDefinition): ApiVersionNbr = {
    sortedVersionsHighestFirst(apiDefinition)
      .headOption.map(apiVersionDefinition => apiVersionDefinition.versionNbr).getOrElse(ApiVersionNbr("1.0"))
  }

  def getAccessTypeOfLatestVersion(apiDefinition: ApiDefinition): ApiAccess = {
    sortedVersionsHighestFirst(apiDefinition)
      .headOption.map(apiVersionDefinition => apiVersionDefinition.access).getOrElse(ApiAccess.PUBLIC)
  }

  def getStatusOfLatestVersion(apiDefinition: ApiDefinition): ApiStatus = {
    sortedVersionsHighestFirst(apiDefinition)
      .headOption.map(apiVersionDefinition => apiVersionDefinition.status).getOrElse(ApiStatus.DEPRECATED)
  }
}

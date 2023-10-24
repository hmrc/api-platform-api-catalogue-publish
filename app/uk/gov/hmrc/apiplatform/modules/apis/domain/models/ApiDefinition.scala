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

package uk.gov.hmrc.apicataloguepublish.apidefinition.models

case class ApiDefinition(
    serviceBaseUrl: String,
    serviceName: String,
    name: String,
    description: String,
    context: ApiContext,
    requiresTrust: Boolean = false,
    isTestSupport: Boolean = false,
    versions: List[ApiVersion],
    categories: List[ApiCategory] = List.empty
  )

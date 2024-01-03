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

package uk.gov.hmrc.apicataloguepublish.apicatalogue.models

import java.util.UUID

import play.api.libs.json.Format

case class IntegrationId(value: UUID) extends AnyVal

object IntegrationId {

  import play.api.libs.json.Json

  implicit val apiIdFormat: Format[IntegrationId] = Json.valueFormat[IntegrationId]
}

//NOTE this model also has platformType returned but we dont care as we know it is API_PLATFORM so no reason to map it.
// We set API_PLATFORM in the connector call so we set tell catalogue what platform we are.
case class PublishResponse(id: IntegrationId, publisherReference: String)

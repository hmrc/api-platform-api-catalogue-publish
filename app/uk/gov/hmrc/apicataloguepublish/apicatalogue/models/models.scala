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
import scala.collection.immutable

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import play.api.libs.json.Format

case class IntegrationId(value: UUID) extends AnyVal

object IntegrationId {

  import play.api.libs.json.Json

  implicit val apiIdFormat: Format[IntegrationId] = Json.valueFormat[IntegrationId]
}

sealed trait PlatformType extends EnumEntry

object PlatformType extends Enum[PlatformType] with PlayJsonEnum[PlatformType] {

  val values: immutable.IndexedSeq[PlatformType] = findValues

  case object DES extends PlatformType

  case object CMA extends PlatformType

  case object CORE_IF extends PlatformType

  case object API_PLATFORM extends PlatformType

  case object CDS_CLASSIC extends PlatformType

  case object TRANSACTION_ENGINE extends PlatformType

}

case class ApiPublishRequest(publisherReference: Option[String], platformType: PlatformType, specificationType: String = "OAS_V3", contents: String)

case class PublishResponse(id: IntegrationId, publisherReference: String, platformType: PlatformType)

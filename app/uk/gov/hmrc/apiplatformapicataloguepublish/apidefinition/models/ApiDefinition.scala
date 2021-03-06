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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models

import cats.data.{NonEmptyList => NEL}
import enumeratum._
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformapicataloguepublish.common.domain.models.ApplicationId

import scala.collection.immutable

case class ApiContext(value: String) extends AnyVal

object ApiContext {
  implicit val apiContextFormat = Json.valueFormat[ApiContext]

  implicit val ordering: Ordering[ApiContext] = new Ordering[ApiContext] {
    override def compare(x: ApiContext, y: ApiContext): Int = x.value.compareTo(y.value)
  }
}

case class ApiVersion(value: String) extends AnyVal

object ApiVersion {
  implicit val apiVersionFormat = Json.valueFormat[ApiVersion]

  implicit val ordering: Ordering[ApiVersion] = new Ordering[ApiVersion] {
    override def compare(x: ApiVersion, y: ApiVersion): Int = x.value.compareTo(y.value)
  }
}

case class ApiIdentifier(context: ApiContext, version: ApiVersion)

object ApiIdentifier {
  implicit val apiIdentifierFormat = Json.format[ApiIdentifier]
}

case class ApiDefinition(
  serviceBaseUrl: String,
    serviceName: String,
    name: String,
    description: String,
    context: ApiContext,
    requiresTrust: Boolean = false,
    isTestSupport: Boolean = false,
    versions: List[ApiVersionDefinition],
    categories: List[ApiCategory] = List.empty)
 

case class ApiCategory(value: String) extends AnyVal

case class ApiVersionDefinition(version: ApiVersion, status: ApiStatus, access: ApiAccess, endpoints: NEL[Endpoint], endpointsEnabled: Boolean = false)

object ApiVersionDefinition {
    implicit val ordering: Ordering[ApiVersionDefinition] = new Ordering[ApiVersionDefinition] {
    override def compare(x: ApiVersionDefinition, y: ApiVersionDefinition): Int = y.version.value.compareTo(x.version.value)
  }
}

sealed trait ApiStatus extends EnumEntry

object ApiStatus extends Enum[ApiStatus] with PlayJsonEnum[ApiStatus] {

  val values: immutable.IndexedSeq[ApiStatus] = findValues
  case object ALPHA extends ApiStatus
  case object BETA extends ApiStatus
  case object STABLE extends ApiStatus
  case object DEPRECATED extends ApiStatus
  case object RETIRED extends ApiStatus
}

sealed trait ApiAccessType extends EnumEntry

object ApiAccessType extends Enum[ApiAccessType] with PlayJsonEnum[ApiAccessType] {

  val values: immutable.IndexedSeq[ApiAccessType] = findValues

  case object PRIVATE extends ApiAccessType
  case object PUBLIC extends ApiAccessType
}

trait ApiAccess
case class PublicApiAccess() extends ApiAccess
case class PrivateApiAccess(allowlistedApplicationIds: List[ApplicationId] = List.empty, isTrial: Boolean = false) extends ApiAccess

case class Endpoint(endpointName: String, uriPattern: String, method: HttpMethod, authType: AuthType, queryParameters: List[Parameter] = List.empty)

sealed trait HttpMethod extends EnumEntry

object HttpMethod extends Enum[HttpMethod] with PlayJsonEnum[HttpMethod] {

  val values: immutable.IndexedSeq[HttpMethod] = findValues

  case object GET extends HttpMethod
  case object POST extends HttpMethod
  case object PUT extends HttpMethod
  case object PATCH extends HttpMethod
  case object DELETE extends HttpMethod
  case object OPTIONS extends HttpMethod
}

sealed trait AuthType extends EnumEntry

object AuthType extends Enum[AuthType] with PlayJsonEnum[AuthType] {

  val values: immutable.IndexedSeq[AuthType] = findValues

  case object NONE extends AuthType
  case object APPLICATION extends AuthType
  case object USER extends AuthType

}

case class Parameter(name: String, required: Boolean = false)



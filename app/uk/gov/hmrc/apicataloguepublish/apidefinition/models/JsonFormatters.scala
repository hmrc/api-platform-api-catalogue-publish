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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiAccessType.{PRIVATE, PUBLIC}
import uk.gov.hmrc.apiplatformapicataloguepublish.common.domain.models._

trait BasicApiDefinitionJsonFormatters extends CommonJsonFormatters {
  implicit val formatApiContext: Format[ApiContext]       = Json.valueFormat[ApiContext]
  implicit val formatApiVersion: Format[ApiVersionNbr]       = Json.valueFormat[ApiVersionNbr]
  implicit val formatApiIdentifier: Format[ApiIdentifier] = Json.format[ApiIdentifier]
  
  implicit val formatApiCategory: Format[ApiCategory]     = Json.valueFormat[ApiCategory]

}

object BasicApiDefinitionJsonFormatters extends BasicApiDefinitionJsonFormatters

trait EndpointJsonFormatters extends NonEmptyListFormatters {
  implicit val formatParameter = Json.format[QueryParameter]

  implicit val endpointReads: Reads[Endpoint] = (
    (JsPath \ "endpointName").read[String] and
      (JsPath \ "uriPattern").read[String] and
      (JsPath \ "method").read[HttpMethod] and
      (JsPath \ "authType").read[AuthType] and
      ((JsPath \ "queryParameters").read[List[QueryParameter]] or Reads.pure(List.empty[QueryParameter]))
  )(Endpoint.apply _)

  implicit val endpointWrites: Writes[Endpoint] = Json.writes[Endpoint]
}

trait ApiDefinitionJsonFormatters extends EndpointJsonFormatters with BasicApiDefinitionJsonFormatters with CommonJsonFormatters {
  import uk.gov.hmrc.apiplatformapicataloguepublish.common.domain.models._

  implicit val apiAccessReads: Reads[ApiAccess] =
    (
      (
        (JsPath \ "type").read[ApiAccessType] and
          ((JsPath \ "isTrial").read[Boolean] or Reads.pure(false))
      ).tupled
    ) map {
      case (PUBLIC, _)     => ApiAccess.PUBLIC
      case (PRIVATE, isTrial) => ApiAccess.Private(isTrial)
    }

  implicit object apiAccessWrites extends Writes[ApiAccess] {

    private val privApiWrites: OWrites[(ApiAccessType,Boolean)] = (
      (JsPath \ "type").write[ApiAccessType] and
        (JsPath \ "isTrial").write[Boolean]
    ).tupled

    override def writes(access: ApiAccess) = access match {
      case ApiAccess.PUBLIC           => Json.obj("type" -> PUBLIC)
      case ApiAccess.Private(isTrial) => privApiWrites.writes((PRIVATE, isTrial))
    }
  }

  implicit val apiVersionReads: Reads[ApiVersion] =
    (
      (
        (JsPath \ "version").read[ApiVersionNbr] and
          (JsPath \ "status").read[ApiStatus] and
          (JsPath \ "access").readNullable[ApiAccess] and
          (JsPath \ "endpoints").read[List[Endpoint]] and
          ((JsPath \ "endpointsEnabled").read[Boolean] or Reads.pure(false))
      ).tupled
    ) map {
      case (version, status, None, endpoints, endpointsEnabled)         => ApiVersion(version, status, ApiAccess.PUBLIC, endpoints, endpointsEnabled)
      case (version, status, Some(access), endpoints, endpointsEnabled) => ApiVersion(version, status, access, endpoints, endpointsEnabled)
    }

  implicit val apiVersionWrites: Writes[ApiVersion] = Json.writes[ApiVersion]

  implicit val apiDefinitionReads: Reads[ApiDefinition] = (
    (JsPath \ "serviceBaseUrl").read[String] and
      (JsPath \ "serviceName").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "context").read[ApiContext] and
      ((JsPath \ "requiresTrust").read[Boolean] or Reads.pure(false)) and
      ((JsPath \ "isTestSupport").read[Boolean] or Reads.pure(false)) and
      (JsPath \ "versions").read[List[ApiVersion]] and
      ((JsPath \ "categories").read[List[ApiCategory]] or Reads.pure(List.empty[ApiCategory]))
  )(ApiDefinition.apply _)

  implicit val apiDefinitionWrites: Writes[ApiDefinition] = Json.writes[ApiDefinition]
}

object ApiDefinitionJsonFormatters extends ApiDefinitionJsonFormatters

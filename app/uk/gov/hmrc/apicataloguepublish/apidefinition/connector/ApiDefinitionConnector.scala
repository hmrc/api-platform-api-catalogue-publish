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

package uk.gov.hmrc.apicataloguepublish.apidefinition.connector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import uk.gov.hmrc.apiplatform.modules.apis.domain.models._
import uk.gov.hmrc.apicataloguepublish.apidefinition.connector.ApiDefinitionConnector._
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionUtils

@Singleton
class ApiDefinitionConnector @Inject() (
    val http: HttpClientV2,
    val config: Config
  )(implicit val ec: ExecutionContext
  ) extends Logging
    with ApiDefinitionUtils {

  private def definitionUrl(serviceName: ServiceName) =
    s"${config.baseUrl}/api-definition/$serviceName"

  private val fetchAllUrl = s"${config.baseUrl}/api-definition"

  def getDefinitionByServiceName(serviceName: ServiceName)(implicit hc: HeaderCarrier): Future[Either[ApiDefinitionFailedResult, ApiDefinitionResult]] = {
    logger.info(s"${this.getClass.getSimpleName} - fetchApiDefinition $serviceName")
    http.get(url"${definitionUrl(serviceName)}")
      .execute[Option[ApiDefinition]]
      .map {
        case Some(x) =>
          logger.info(s"${this.getClass.getSimpleName} - fetchApiDefinition $serviceName Successful")
          Right(definitionToResult(x))
        case _       =>
          logger.warn(s"${this.getClass.getSimpleName} - fetchApiDefinition $serviceName Failed")
          Left(NotFoundResult(s"unable to fetch definition: $serviceName"))
      }.recover {
        case NonFatal(e) =>
          logger.error(s"Failed to getDefinitionByServiceName: $serviceName ", e)
          Left(GeneralFailedResult(e.getMessage))
      }
  }

  private def definitionToResult(definition: ApiDefinition): ApiDefinitionResult = {
    ApiDefinitionResult(getUri(definition), getAccessTypeOfLatestVersion(definition), definition.serviceName, getStatusOfLatestVersion(definition))
  }

  def getAllServices()(implicit hc: HeaderCarrier): Future[Either[GeneralFailedResult, List[ApiDefinitionResult]]] = {
    http.get(url"$fetchAllUrl?type=all")
      .execute[Seq[ApiDefinition]]
      .map(definitions =>
        Right(definitions.map(definitionToResult).toList.sortBy(_.serviceName))
      ).recover {
        case NonFatal(e) =>
          logger.error(s"getAllServices Failed:", e)
          Left(GeneralFailedResult(e.getMessage))
      }
  }

}

object ApiDefinitionConnector {
  case class Config(baseUrl: String)
  case class ApiDefinitionResult(url: String, access: ApiAccess, serviceName: ServiceName, status: ApiStatus)

  sealed trait ApiDefinitionFailedResult {
    val message: String
  }
  case class NotFoundResult(message: String)      extends ApiDefinitionFailedResult
  case class GeneralFailedResult(message: String) extends ApiDefinitionFailedResult

}

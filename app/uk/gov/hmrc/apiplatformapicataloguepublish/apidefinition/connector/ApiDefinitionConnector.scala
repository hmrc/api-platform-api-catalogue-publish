/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector

import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{ApiAccess, ApiDefinition, ApiDefinitionJsonFormatters}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.http.ws.WSGet

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiStatus

@Singleton
class ApiDefinitionConnector @Inject() (
    val http: HttpClient with WSGet,
    val config: Config
  )(implicit val ec: ExecutionContext)
    extends Logging
    with ApiDefinitionJsonFormatters
    with ApiDefinitionUtils {

  private def definitionUrl(serviceName: String) =
    s"${config.baseUrl}/api-definition/$serviceName"

  private val fetchAllUrl = s"${config.baseUrl}/api-definition"

  def getDefinitionByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ApiDefinitionFailedResult, ApiDefinitionResult]] = {
    logger.info(s"${this.getClass.getSimpleName} - fetchApiDefinition")
    http.GET[Option[ApiDefinition]](definitionUrl(serviceName)).map {
      case Some(x) => Right(definitionToResult(x))
      case _       => Left(NotFoundResult(s"unable to fetch definition: $serviceName"))
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
    http.GET[Seq[ApiDefinition]](fetchAllUrl, List(("type", "all")))
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
  case class ApiDefinitionResult(url: String, access: ApiAccess, serviceName: String, status: ApiStatus)

  sealed trait ApiDefinitionFailedResult {
    val message: String
  }
  case class NotFoundResult(message: String) extends ApiDefinitionFailedResult
  case class GeneralFailedResult(message: String) extends ApiDefinitionFailedResult


}



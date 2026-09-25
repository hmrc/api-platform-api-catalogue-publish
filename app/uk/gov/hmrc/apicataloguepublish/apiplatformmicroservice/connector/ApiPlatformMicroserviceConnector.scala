/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import cats.data.EitherT
import org.apache.pekko.stream.Materializer

import play.api.Logging
import play.api.http.HttpEntity
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.OFormat
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException, StringContextOps}

import uk.gov.hmrc.apiplatform.modules.apis.domain.models.{ApiAccessType, ApiDefinition, ApiStatus, Locator, ServiceName}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiVersionNbr, Environment}
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector.ApiPlatformMicroserviceConnector._

@Singleton
class ApiPlatformMicroserviceConnector @Inject() (
    val http: HttpClientV2,
    val ws: WSClient,
    val config: Config
  )(implicit val ec: ExecutionContext,
    mat: Materializer
  ) extends Logging
    with ApiDefinitionUtils {

  def fetchApiForServiceName(serviceName: ServiceName)(implicit hc: HeaderCarrier): Future[Either[ApiDefinitionFailedResult, ApiDefinitionResult]] = {
    implicit val locatorFormatter: OFormat[Locator[ApiDefinition]] = Locator.buildLocatorFormatter[ApiDefinition]
    http.get(url"${config.baseUrl}/api-definitions/service-name/$serviceName")
      .execute[Option[Locator[ApiDefinition]]]
      .map {
        case Some(locator) =>
          logger.info(s"${this.getClass.getSimpleName} - fetchApiForServiceName $serviceName Successful")
          Right(definitionToResult(locator))
        case _             =>
          logger.warn(s"${this.getClass.getSimpleName} - fetchApiForServiceName $serviceName Failed")
          Left(NotFoundResult(s"unable to fetch definition: $serviceName"))
      }.recover {
        case NonFatal(e) =>
          logger.error(s"Failed to getDefinitionByServiceName: $serviceName ", e)
          Left(GeneralFailedResult(e.getMessage))
      }
  }

  private def definitionToResult(locator: Locator[ApiDefinition]): ApiDefinitionResult = {
    val (environment, definition: ApiDefinition) = locator match {
      case Locator.Sandbox(sandbox)       => (Environment.SANDBOX, sandbox)
      case Locator.Production(production) => (Environment.PRODUCTION, production)
      case Locator.Both(_, production)    => (Environment.PRODUCTION, production)
    }
    ApiDefinitionResult(environment, getAccessTypeOfLatestVersion(definition), definition.serviceName, getLatestVersion(definition), getStatusOfLatestVersion(definition))
  }

  def fetchApiDocumentationResource(environment: Environment, serviceName: ServiceName, version: ApiVersionNbr, resource: String): Future[Either[Throwable, String]] = {
    val url = url"${config.baseUrl}/environment/$environment/$serviceName/$version/documentation/$resource".toString()
    ws.url(url).withMethod("GET").stream().flatMap {
      streamedResponse =>
        streamedResponse.status match {
          case OK          => EitherT.liftF(convertStreamToYamlString(streamedResponse)).value
          case NOT_FOUND   =>
            logger.error(s"API microservice resource by URL: $url not found")
            Future.successful(Left(new NotFoundException(s"Resource not found - $url")))
          case status: Int =>
            logger.error(s"Error downloading resource - $url status returned : $status")
            Future.successful(Left(new InternalServerException(s"Error downloading resource - $url")))
        }
    }

  }

  private def convertStreamToYamlString(response: WSResponse): Future[String] = {
    val contentType = response.headers.get("Content-Type").flatMap(_.headOption)
      .getOrElse("application/octet-stream")

    (response.headers.get("Content-Length") match {
      case Some(Seq(length)) =>
        HttpEntity.Streamed(response.bodyAsSource, Some(length.toLong), Some(contentType))
      case _                 =>
        HttpEntity.Streamed(response.bodyAsSource, None, Some(contentType))
    }).consumeData
      .map(byteString => byteString.decodeString("UTF-8"))
  }
}

object ApiPlatformMicroserviceConnector {
  case class Config(baseUrl: String)
  case class ApiDefinitionResult(environment: Environment, access: ApiAccessType, serviceName: ServiceName, version: ApiVersionNbr, status: ApiStatus)

  sealed trait ApiDefinitionFailedResult {
    val message: String
  }
  case class NotFoundResult(message: String)      extends ApiDefinitionFailedResult
  case class GeneralFailedResult(message: String) extends ApiDefinitionFailedResult

}

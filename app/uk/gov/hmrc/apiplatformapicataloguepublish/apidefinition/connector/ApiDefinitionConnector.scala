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

 @Singleton
 class ApiDefinitionConnector @Inject()(
   val http: HttpClient with WSGet,
   val config: Config)(implicit val ec: ExecutionContext) extends Logging with ApiDefinitionJsonFormatters with ApiDefinitionUtils {

    private def definitionUrl(serviceBaseUrl: String, serviceName: String) =
      s"$serviceBaseUrl/api-definition/$serviceName"

    def getDefinitionByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ApiDefinitionFailedResult, ApiDefinitionResult]] = {
      logger.info(s"${this.getClass.getSimpleName} - fetchApiDefinition")
      http.GET[Option[ApiDefinition]](definitionUrl(config.baseUrl, serviceName)).map{
        case Some(x) => Right(ApiDefinitionResult(getRamlUri(x), getAccessTypeOfLatestVersion(x), serviceName))
        case _ => Left(ApiDefinitionNotFoundResult(" unable to fetch definition"))
      }.recover {
        case NonFatal(e)          =>
          logger.error(s"Failed", e)
          Left(ApiDefinitionGeneralFailedResult(e.getMessage))
      }
    }
  
}

object ApiDefinitionConnector {
  case class Config(baseUrl: String)
  case class ApiDefinitionResult(url: String, access: ApiAccess, serviceName: String)

  sealed trait ApiDefinitionFailedResult {
    val message: String
  }
  case class ApiDefinitionNotFoundResult(message: String) extends  ApiDefinitionFailedResult
  case class ApiDefinitionGeneralFailedResult(message: String) extends  ApiDefinitionFailedResult
}
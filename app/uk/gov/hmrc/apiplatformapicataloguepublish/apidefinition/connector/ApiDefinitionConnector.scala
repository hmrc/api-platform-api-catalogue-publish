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



import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{ApiDefinition, ApiDefinitionJsonFormatters}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.Config

import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import uk.gov.hmrc.play.http.ws.WSGet

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import uk.gov.hmrc.http.HttpReads.Implicits._

 @Singleton
 class ApiDefinitionConnector @Inject()(
   val http: HttpClient with WSGet,
   val config: Config)(implicit val ec: ExecutionContext) extends Logging with ApiDefinitionJsonFormatters {

    private def definitionUrl(serviceBaseUrl: String, serviceName: String) =
      s"$serviceBaseUrl/api-definition/$serviceName"

    def getDefinitionByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, ApiDefinition]] = {
      logger.info(s"${this.getClass.getSimpleName} - fetchApiDefinition")
      val r = http.GET[Option[ApiDefinition]](definitionUrl(config.baseUrl, serviceName)).map{
        case Some(x) => Right(x)
        case _ => Left(new NotFoundException(" unable to fetch definition"))
      }

      r.recover {
        case NonFatal(e)          =>
          logger.error(s"Failed $e")
         Left(e)
      }
    }
  
}

object ApiDefinitionConnector {
  case class Config(baseUrl: String)
}
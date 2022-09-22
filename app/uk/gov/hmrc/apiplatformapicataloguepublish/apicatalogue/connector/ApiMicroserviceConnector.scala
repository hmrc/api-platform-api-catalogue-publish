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

package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiMicroserviceConnector @Inject()(ws: WSClient)(implicit val ec: ExecutionContext) extends Logging {

  def fetchApiDocumentationResourceByUrl(url: String): Future[Either[Throwable, String]] = {
    logger.info(s"Calling local microservice to fetch resource by URL: $url")
    ws.url(url).withMethod("GET").stream().map {
      streamedResponse =>

      streamedResponse.status match {
        case OK => Right(streamedResponse.body)
        case NOT_FOUND => Left(new NotFoundException(s"Resource not found - $url"))
        case _ => Left(new InternalServerException(s"Error downloading resource - $url"))
      }
    }

  }
}

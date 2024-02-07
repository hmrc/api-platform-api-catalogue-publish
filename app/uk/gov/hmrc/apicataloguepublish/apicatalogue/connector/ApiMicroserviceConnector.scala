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

package uk.gov.hmrc.apicataloguepublish.apicatalogue.connector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import org.apache.pekko.stream.Materializer

import play.api.Logging
import play.api.http.HttpEntity
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}

@Singleton
class ApiMicroserviceConnector @Inject() (ws: WSClient)(implicit val ec: ExecutionContext, implicit val mat: Materializer) extends Logging {

  def fetchApiDocumentationResourceByUrl(url: String): Future[Either[Throwable, String]] = {
    logger.info(s"Calling local microservice to fetch resource by URL: $url")
    ws.url(url).withMethod("GET").stream().flatMap {
      streamedResponse =>
        streamedResponse.status match {
          case OK        => EitherT.liftF(convertStreamToYamlString(streamedResponse)).value
          case NOT_FOUND => Future.successful(Left(new NotFoundException(s"Resource not found - $url")))
          case _         => Future.successful(Left(new InternalServerException(s"Error downloading resource - $url")))
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

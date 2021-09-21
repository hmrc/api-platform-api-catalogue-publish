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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.service

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.{ApiDefinitionConnector, ApiRamlParser}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinition

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import play.api.http.Status._
import play.api.libs.ws.WSResponse
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.http.HttpEntity
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.http.InternalServerException

@Singleton()
class ApiDefinitionService @Inject() (apiDefinitionConnector: ApiDefinitionConnector, apiRamlParser: ApiRamlParser)(implicit val ec: ExecutionContext) {

    val PROXY_SAFE_CONTENT_TYPE = "Proxy-Safe-Content-Type"

  def getDefinitionByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, String]] = {

    for {
      apiDefinitionResult <- apiDefinitionConnector.getDefinitionByServiceName(serviceName)
      ramlString <- handleApiDefinitionResult(apiDefinitionResult)
    } yield ramlString

    //get raml from api producer microservice (how do we determine the link for this?)
    // convert raml to OAS and add our api catalogue specific items.
    // publish api on api catalogue
  }

  // private def handleFetchResourceStream(serviceName: String)(implicit hc: HeaderCarrier) = {

  //   def createProxySafeContentType(contentType: String): (String, String) = (PROXY_SAFE_CONTENT_TYPE, contentType)
  //   for {
  //      apiDefinition <- apiDefinitionConnector.getDefinitionByServiceName(serviceName)
  //     streamedResponse <- getRamlString(apiDefinition.right.get)
  //   } yield streamedResponse.status match {
  //     case OK        =>
  //       val contentType = streamedResponse.contentType

  //       streamedResponse.headers.get("Content-Length") match {
  //         case Some(Seq(length)) => Ok.sendEntity(HttpEntity.Streamed(streamedResponse.bodyAsSource, Some(length.toLong), Some(contentType)))
  //             .withHeaders(createProxySafeContentType(contentType))

  //         case _                 => Ok.chunked(streamedResponse.bodyAsSource).as(contentType)
  //             .withHeaders(createProxySafeContentType(contentType))
  //       }
  //     case NOT_FOUND => throw new NotFoundException(serviceName)
  //     case status    => throw new InternalServerException(serviceName)
  //   }
  // }

  private def handleApiDefinitionResult(apiDefinitionResult: Either[Throwable, ApiDefinition])(implicit hc: HeaderCarrier): Future[Either[Throwable, String]] = {
    apiDefinitionResult match {
      case Right(apiDefinition) => getRamlString(apiDefinition)
      case Left(e)              => Future.successful(Left(e))
    }
  }

  private def getRamlString(apiDefinition: ApiDefinition)(implicit hc: HeaderCarrier) = {
    apiRamlParser
      .getRaml(getBaseUrl(apiDefinition)+s"/api/conf/${getLatestVersion(apiDefinition)}/application.raml")
      .map(x => Right(x.raw.get.toString))

  }

  private def getBaseUrl(apiDefinition: ApiDefinition): String = {
    //apiDefinition.serviceBaseUrl
    s"http://localhost:9820" // customs-declarations running locally
  }

  private def getLatestVersion(apiDefinition: ApiDefinition): String = {
    //apiDefinition.versions.headOption.map(apiVersionDefinition => apiVersionDefinition.version.value).getOrElse("1.0")
    "1.0"

  }
}

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

package uk.gov.hmrc.apiplatformapicataloguepublish.service

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.ApiRamlParser
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.OasParser
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinition

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import webapi.WebApiDocument




@Singleton()
class PublishService @Inject() (apiDefinitionConnector: ApiDefinitionConnector, apiRamlParser: ApiRamlParser, oasParser: OasParser)(implicit val ec: ExecutionContext) {

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, String]] = {

    val result = for {
      apiDefinitionResult <- apiDefinitionConnector.getDefinitionByServiceName(serviceName)
      webApiDocument <- handleApiDefinitionResult(apiDefinitionResult)
    } yield (webApiDocument, apiDefinitionResult)

    result.flatMap {
      case (Right(webApiDocument), Right(apiDefinition: ApiDefinition)) => {
       oasParser.parseWebApiDocument(serviceName, ApiDefinition.getAccessTypeOfLatestVersion(apiDefinition), webApiDocument)
      }
      case _ => Future.successful(Left(new RuntimeException(""))) 
    }
  }

  private def handleApiDefinitionResult(apiDefinitionResult: Either[Throwable, ApiDefinition]): Future[Either[Throwable, WebApiDocument]] = {
    apiDefinitionResult match {
      case Right(apiDefinition) => apiRamlParser.getRaml(ApiDefinition.getRamlUri(apiDefinition)).map(Right(_))
      case Left(e)              => Future.successful(Left(e))
    }
  }

}

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


@Singleton()
class PublishService @Inject() (apiDefinitionConnector: ApiDefinitionConnector, apiRamlParser: ApiRamlParser)(implicit val ec: ExecutionContext) {

    val PROXY_SAFE_CONTENT_TYPE = "Proxy-Safe-Content-Type"

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, String]] = {

    for {
      apiDefinitionResult <- apiDefinitionConnector.getDefinitionByServiceName(serviceName)
      ramlString <- handleApiDefinitionResult(apiDefinitionResult)
    } yield ramlString

  }

  private def handleApiDefinitionResult(apiDefinitionResult: Either[Throwable, ApiDefinition]): Future[Either[Throwable, String]] = {
    apiDefinitionResult match {
      case Right(apiDefinition) => getRamlString(apiDefinition)
      case Left(e)              => Future.successful(Left(e))
    }
  }

  private def getRamlString(apiDefinition: ApiDefinition) = {
    apiRamlParser
      .getRaml(ApiDefinition.getRamlUri(apiDefinition))
      .map(x => Right(x.raw.get.toString))

  }
}

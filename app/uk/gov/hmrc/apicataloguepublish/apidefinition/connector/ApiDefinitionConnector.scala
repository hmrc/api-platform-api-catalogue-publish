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
import uk.gov.hmrc.apiplatform.modules.common.domain.models.Environment
import uk.gov.hmrc.apicataloguepublish.apidefinition.connector.ApiDefinitionConnector._
import uk.gov.hmrc.apicataloguepublish.apidefinition.utils.ApiDefinitionUtils
import uk.gov.hmrc.apicataloguepublish.apiplatformmicroservice.connector.ApmConnector

@Singleton
class ApiDefinitionConnector @Inject() (
    val http: HttpClientV2,
    val config: Config
  )(implicit val ec: ExecutionContext
  ) extends Logging
    with ApiDefinitionUtils {

  private val fetchAllUrl = s"${config.baseUrl}/api-definition"

  private def definitionToResult(definition: ApiDefinition): ApmConnector.Result = {
    ApmConnector.Result(
      Environment.PRODUCTION,
      getAccessTypeOfLatestVersion(definition),
      definition.serviceName,
      getLatestVersion(definition),
      getStatusOfLatestVersion(definition)
    )
  }

  def getAllServices()(implicit hc: HeaderCarrier): Future[Either[ApmConnector.GeneralFailedResult, List[ApmConnector.Result]]] = {
    http.get(url"$fetchAllUrl?type=all")
      .execute[Seq[ApiDefinition]]
      .map(definitions =>
        Right(definitions.map(definitionToResult).toList.sortBy(_.serviceName))
      ).recover {
        case NonFatal(e) =>
          logger.error(s"getAllServices Failed:", e)
          Left(ApmConnector.GeneralFailedResult(e.getMessage))
      }
  }

}

object ApiDefinitionConnector {
  case class Config(baseUrl: String)
}

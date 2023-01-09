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

package uk.gov.hmrc.apiplatformapicataloguepublish.service

import cats.data.EitherT
import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.ApiDefinitionResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiAccess
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiAccess.apiAccessToDescription
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.OasResult
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, Oas30Wrapper}
import webapi.WebApiDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class Raml2OasService @Inject() (oas30Wrapper: Oas30Wrapper, apiRamlParser: ApiRamlParser)(implicit ec: ExecutionContext) extends Logging {

  def getRamlAndConvert(apiDefinitionResult: ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, OasResult]] = {
    (for {
      ramlAndDefinition <- EitherT(getRamlForApiDefinition(apiDefinitionResult))
      convertedOas      <- EitherT(handleRamlToOas(ramlAndDefinition))
    } yield convertedOas).value
  }

  def getRamlForApiDefinition(apiDefinitionResult: ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, ResultHolder]] = {
    logger.info(s"getRamlForApiDefinition called for ${apiDefinitionResult.serviceName}")
    apiRamlParser.getRaml(apiDefinitionResult.url + ".raml")
      .map(x => Right(ResultHolder(apiDefinitionResult, x)))
      .recover {
        case NonFatal(e: Throwable) =>
          logger.error("getRamlForApiDefinition failed: ", e)
          Left(PublishFailedResult(apiDefinitionResult.serviceName, s"getRamlForApiDefinition failed: ${e.getMessage}"))
      }
  }

  def handleRamlToOas(resultHolder: ResultHolder): Future[Either[ApiCataloguePublishResult, OasResult]] = {
    logger.info(s"handleRamlToOas called for ${resultHolder.apiDefinitionResult.serviceName}")
    parseWebApiDocument(resultHolder.document, resultHolder.apiDefinitionResult.serviceName, resultHolder.apiDefinitionResult.access)
      .map(Right(_))
      .recover {
        case NonFatal(e: Throwable) =>
          logger.error("handleRamlToOas failed: ", e)
          Left(PublishFailedResult(resultHolder.apiDefinitionResult.serviceName, s"handleRamlToOas failed: ${e.getMessage}"))
      }
  }

  protected[service] def parseWebApiDocument(model: WebApiDocument, apiName: String, accessType: ApiAccess): Future[OasResult] = {
    val startTime = System.currentTimeMillis()
    val result    = oas30Wrapper.ramlToOas(model)
      .map(oasAsString => OasResult(oasAsString, apiName, apiAccessToDescription(accessType)))
    logger.info(s"ramlToOas completed for $apiName and took ${System.currentTimeMillis() - startTime} milliseconds")
    result
  }

}

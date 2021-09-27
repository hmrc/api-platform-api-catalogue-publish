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
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.ApiDefinitionResult
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.ApiRamlParser
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.OasParser

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import webapi.WebApiDocument
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.ConvertedWebApiToOasResult
import uk.gov.hmrc.http.Upstream4xxResponse

import cats.data.EitherT
import cats.implicits._
import scala.util.control.NonFatal
import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.GeneralOpenApiProcessingError

@Singleton()
class PublishService @Inject() (apiDefinitionConnector: ApiDefinitionConnector,
                                apiRamlParser: ApiRamlParser, oasParser: OasParser)(implicit val ec: ExecutionContext) extends Logging {

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ParsedResult, String]] = {
    val result = for {
      apiDefinitionResult <- EitherT(apiDefinitionConnector.getDefinitionByServiceName(serviceName).map(mapApiDefinitionResult))
      ramlAndDefinition <- EitherT(getRamlForApiDefinition(apiDefinitionResult))
      convertedOas <- EitherT(handleRamlToOas(ramlAndDefinition))
      result <- EitherT(handleEnhancingOasForCatalogue(convertedOas))
    } yield result
    result.value
  }

  def mapApiDefinitionResult(result: Either[Throwable, ApiDefinitionResult]): Either[ParsedResult, ApiDefinitionResult] = {
    result match {
      case Right(x: ApiDefinitionResult)                     => Right(x)
      case Left(e: Upstream4xxResponse) => Left(ApiDefinitionNotFoundResult(e.getMessage))
      case _                            => Left(PublishFailedResult("Unknown error occured"))
    }

  }

  private def getRamlForApiDefinition(apiDefinitionResult:ApiDefinitionResult): Future[Either[ParsedResult, ResultHolder]] = {
    apiRamlParser.getRaml(apiDefinitionResult.url)
    .map(x => Right(ResultHolder(apiDefinitionResult, x)))
    .recover {
      case NonFatal(e: Throwable) => logger.error("getRamlForApiDefinition failed: ", e)
      Left(PublishFailedResult(s"getRamlForApiDefinition failed: ${e.getMessage}"))
    }
  }

  def handleRamlToOas(ResultHolder: ResultHolder): Future[Either[ParsedResult, ConvertedWebApiToOasResult]] = {
    oasParser.parseWebApiDocument(ResultHolder.document, ResultHolder.apiDefinitionResult.serviceName, ResultHolder.apiDefinitionResult.access)
    .map(Right(_))
    .recover {
      case NonFatal(e: Throwable) => logger.error("handleRamlToOas failed: ", e)
      Left(PublishFailedResult(s"handleRamlToOas failed: ${e.getMessage}"))
    }
  }

  def handleEnhancingOasForCatalogue(oasResult: ConvertedWebApiToOasResult): Future[Either[ParsedResult, String]] ={
      oasParser.enhanceOas(oasResult) match {
        case Right(value: String) => Future.successful(Right(value))
        case Left(e: GeneralOpenApiProcessingError) => Future.successful(Left(OpenApiEnhancementFailedResult(s"handleEnhancingOasForCatalogue failed: ${e.message}")))
      }
  }

}

case class ResultHolder(apiDefinitionResult: ApiDefinitionResult, document: WebApiDocument)

sealed trait ParsedResult

case class ApiDefinitionNotFoundResult(message: String) extends ParsedResult
case class PublishFailedResult(message: String) extends ParsedResult
case class OpenApiEnhancementFailedResult(message: String) extends ParsedResult

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

import cats.data.EitherT
import cats.implicits._
import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueFailedResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PublishResponse
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector._
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{ConvertedWebApiToOasResult, GeneralOpenApiProcessingError}
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, OasParser}
import uk.gov.hmrc.http.HeaderCarrier
import webapi.WebApiDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton()
class PublishService @Inject() (apiDefinitionConnector: ApiDefinitionConnector,
                                apiRamlParser: ApiRamlParser,
                                 oasParser: OasParser,
                                 catalogueConnector: ApiCatalogueAdminConnector)(implicit val ec: ExecutionContext) extends Logging {

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ApiCataloguePublishResult, PublishResponse]] = {
    val result = for {
      apiDefinitionResult     <- EitherT(apiDefinitionConnector.getDefinitionByServiceName(serviceName).map(mapApiDefinitionResult))
      ramlAndDefinition       <- EitherT(getRamlForApiDefinition(apiDefinitionResult))
      convertedOas            <- EitherT(handleRamlToOas(ramlAndDefinition))
      oasDataWithExtensions   <- EitherT(handleEnhancingOasForCatalogue(convertedOas))
      result                  <- EitherT(catalogueConnector.publishApi(oasDataWithExtensions).map(mapCataloguePublishResult))
    } yield result
    result.value
  }

  def mapCataloguePublishResult(result: Either[ApiCatalogueFailedResult, PublishResponse]): Either[ApiCataloguePublishResult, PublishResponse] = {
    result match {
      case Right(response: PublishResponse) => Right(response)
      case Left(e : ApiCatalogueFailedResult)  => logger.error(s"publish to catalogue failed ${e.message}")
        Left(ApiCataloguePublishFailedResult(s"publish to catalogue failed ${e.message}"))
    }
  }

  def mapApiDefinitionResult(result: Either[ApiDefinitionFailedResult, ApiDefinitionResult]): Either[ApiCataloguePublishResult, ApiDefinitionResult] = {
    result match {
      case Right(x: ApiDefinitionResult)                     => Right(x)
      case Left(e: ApiDefinitionConnector.ApiDefinitionNotFoundResult) => Left(ApiDefinitionNotFoundResult(e.message))
      case Left(e: ApiDefinitionFailedResult)                            => Left(PublishFailedResult(e.message))
    }

  }

  private def getRamlForApiDefinition(apiDefinitionResult:ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, ResultHolder]] = {
    apiRamlParser.getRaml(apiDefinitionResult.url)
    .map(x => Right(ResultHolder(apiDefinitionResult, x)))
    .recover {
      case NonFatal(e: Throwable) => logger.error("getRamlForApiDefinition failed: ", e)
      Left(PublishFailedResult(s"getRamlForApiDefinition failed: ${e.getMessage}"))
    }
  }

  def handleRamlToOas(ResultHolder: ResultHolder): Future[Either[ApiCataloguePublishResult, ConvertedWebApiToOasResult]] = {
    oasParser.parseWebApiDocument(ResultHolder.document, ResultHolder.apiDefinitionResult.serviceName, ResultHolder.apiDefinitionResult.access)
    .map(Right(_))
    .recover {
      case NonFatal(e: Throwable) => logger.error("handleRamlToOas failed: ", e)
      Left(PublishFailedResult(s"handleRamlToOas failed: ${e.getMessage}"))
    }
  }

  def handleEnhancingOasForCatalogue(oasResult: ConvertedWebApiToOasResult): Future[Either[ApiCataloguePublishResult, String]] ={
      oasParser.enhanceOas(oasResult) match {
        case Right(value: String) => Future.successful(Right(value))
        case Left(e: GeneralOpenApiProcessingError) =>
          Future.successful(Left(OpenApiEnhancementFailedResult(s"handleEnhancingOasForCatalogue failed: ${e.message}")))
      }
  }

}

case class ResultHolder(apiDefinitionResult: ApiDefinitionResult, document: WebApiDocument)

sealed trait ApiCataloguePublishResult

case class ApiDefinitionNotFoundResult(message: String) extends ApiCataloguePublishResult
case class PublishFailedResult(message: String) extends ApiCataloguePublishResult
case class OpenApiEnhancementFailedResult(message: String) extends ApiCataloguePublishResult
case class ApiCataloguePublishFailedResult(message: String) extends ApiCataloguePublishResult

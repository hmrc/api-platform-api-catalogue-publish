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

package uk.gov.hmrc.apiplatformapicataloguepublish.service

import cats.data.EitherT
import cats.implicits._
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.{ApiCatalogueAdminConnector, ApiMicroserviceConnector}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.ApiCatalogueFailedResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PublishResponse
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector._
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.{ApiDefinition, ApiStatus}
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{GeneralOpenApiProcessingError, OasResult}
import uk.gov.hmrc.apiplatformapicataloguepublish.parser.{ApiRamlParser, OasParser}
import uk.gov.hmrc.http.HeaderCarrier
import webapi.WebApiDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton()
class PublishService @Inject() (
    apiDefinitionConnector: ApiDefinitionConnector,
    apiRamlParser: ApiRamlParser,
    oasParser: OasParser,
    catalogueConnector: ApiCatalogueAdminConnector,
    apiMicroserviceConnector: ApiMicroserviceConnector
  )(implicit val ec: ExecutionContext)
    extends Logging {

  val BATCH_AMOUNT = 5

  def getRamlUrl(apiDefinitionResult: ApiDefinitionResult) ={
    apiDefinitionResult.url+".raml"
  }
  def getYamlUrl(apiDefinitionResult: ApiDefinitionResult) ={
    apiDefinitionResult.url+".yaml"
  }

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ApiCataloguePublishResult, PublishResponse]] = {
    (for {
      apiDefinitionResult <- EitherT(apiDefinitionConnector.getDefinitionByServiceName(serviceName).map(mapApiDefinitionResult(_, serviceName)))
      result <- publishDefinitionResult(apiDefinitionResult)
    } yield result).value
  }

  def publishDefinitionResult(apiDefinitionResult: ApiDefinitionResult): EitherT[Future, ApiCataloguePublishResult, PublishResponse] = {
    val serviceName = apiDefinitionResult.serviceName
    logger.info(s"publishDefinitionResult START for $serviceName")
    apiDefinitionResult.status match {
      case ApiStatus.RETIRED =>
        EitherT.left(Future.successful(ApiDefinitionInvalidStatusResult(apiDefinitionResult.serviceName, "definition record was RETIRED for this service")))
      case _                 => for {
          oasValue <- EitherT(ramlOrYaml(apiDefinitionResult))
          oasDataWithExtensions <- EitherT(handleEnhancingOasForCatalogue(oasValue))
          result <- EitherT(catalogueConnector.publishApi(oasDataWithExtensions).map(mapCataloguePublishResult(_, serviceName)))
        } yield result
    }

  }

  def ramlOrYaml(apiDefinitionResult: ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, OasResult]] = {

    def getYaml(apiDefinitionResult: ApiDefinitionResult): Future[Either[Throwable, String]] ={
      apiMicroserviceConnector.fetchApiDocumentationResourceByUrl(getYamlUrl(apiDefinitionResult))
    }

    def handleYamlResult(result: Either[Throwable, String]): Future[Either[ApiCataloguePublishResult, OasResult]] ={
      result match {
        case Left(_) =>  handleRaml(apiDefinitionResult)
        case Right(oas: String) => Future.successful(Right(OasResult(
          oas,
          apiDefinitionResult.serviceName,
          oasParser.accessTypeDescription(apiDefinitionResult.access))))
      }
    }
    for {
      yamlResult <- getYaml(apiDefinitionResult)
      handledResult <- handleYamlResult(yamlResult)
    }yield handledResult

  }

  def handleRaml(apiDefinitionResult: ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, OasResult]] ={
    (for {
      ramlAndDefinition <- EitherT(getRamlForApiDefinition(apiDefinitionResult))
      convertedOas <- EitherT(handleRamlToOas(ramlAndDefinition))
    } yield convertedOas).value
  }

  def publishAll()(implicit hc: HeaderCarrier): Future[List[Either[ApiCataloguePublishResult, PublishResponse]]] = {
    apiDefinitionConnector.getAllServices
      .flatMap {
        case Right(definitionList: List[ApiDefinitionResult]) => 
          batchFutures(definitionList, List.empty)
        case Left(_: GeneralFailedResult)                     =>
          Future.successful(List(Left(PublishFailedResult("All Services", "something went wrong calling api definition"))))
      }

  }

  def batchFutures(
      input: Seq[ApiDefinitionResult],
      results: List[Either[ApiCataloguePublishResult, PublishResponse]]
    )(implicit ec: ExecutionContext
    ): Future[List[Either[ApiCataloguePublishResult, PublishResponse]]] = {
    val startTime = System.currentTimeMillis()
    input.splitAt(BATCH_AMOUNT) match {
      case (Nil, Nil)                                                           => Future.successful(results)
      case (doNow: Seq[ApiDefinitionResult], doLater: Seq[ApiDefinitionResult]) =>
        Future.sequence(doNow.map(publishDefinitionResult(_).value)).flatMap(newResults => {
          logger.info(s"batchFutures - Done batch of items ${doNow.map(_.serviceName).mkString(" - ")}")
          val totalTime = System.currentTimeMillis() - startTime
          logger.info(s"batchFutures took $totalTime milliseconds")
          batchFutures(doLater, results ++ newResults)
        })
    }
  }

  def mapCataloguePublishResult(result: Either[ApiCatalogueFailedResult, PublishResponse],
                                serviceName: String): Either[ApiCataloguePublishResult, PublishResponse] = {
    logger.info(s"mapCataloguePublishResult called for $serviceName")
    result match {
      case Right(response: PublishResponse)  => Right(response)
      case Left(e: ApiCatalogueFailedResult) => logger.error(s"publish to catalogue failed ${e.message}")
        Left(ApiCataloguePublishFailedResult(serviceName, s"publish to catalogue failed ${e.message}"))
    }
  }

  def mapApiDefinitionResult(result: Either[ApiDefinitionFailedResult, ApiDefinitionResult],
                             serviceName: String): Either[ApiCataloguePublishResult, ApiDefinitionResult] =
    result match {
      case Right(x: ApiDefinitionResult)                  => Right(x)
      case Left(e: ApiDefinitionConnector.NotFoundResult) =>
        logger.error(s"Api definition not found: ${e.message}")
        Left(ApiDefinitionNotFoundResult(serviceName, e.message))
      case Left(e: ApiDefinitionFailedResult)             =>
        logger.error(s"Api definition failed: ${e.message}")
        Left(PublishFailedResult(serviceName, e.message))
    }

  private def getRamlForApiDefinition(apiDefinitionResult: ApiDefinitionResult): Future[Either[ApiCataloguePublishResult, ResultHolder]] = {
    logger.info(s"getRamlForApiDefinition called for ${apiDefinitionResult.serviceName}")
    apiRamlParser.getRaml(getRamlUrl(apiDefinitionResult))
      .map(x => Right(ResultHolder(apiDefinitionResult, x)))
      .recover {
        case NonFatal(e: Throwable) => logger.error("getRamlForApiDefinition failed: ", e)
          Left(PublishFailedResult(apiDefinitionResult.serviceName, s"getRamlForApiDefinition failed: ${e.getMessage}"))
      }
  }

  def handleRamlToOas(resultHolder: ResultHolder): Future[Either[ApiCataloguePublishResult, OasResult]] = {
    logger.info(s"handleRamlToOas called for ${resultHolder.apiDefinitionResult.serviceName}")
    oasParser.parseWebApiDocument(resultHolder.document, resultHolder.apiDefinitionResult.serviceName, resultHolder.apiDefinitionResult.access)
      .map(Right(_))
      .recover {
        case NonFatal(e: Throwable) => logger.error("handleRamlToOas failed: ", e)
          Left(PublishFailedResult(resultHolder.apiDefinitionResult.serviceName, s"handleRamlToOas failed: ${e.getMessage}"))
      }
  }

  def handleEnhancingOasForCatalogue(oasResult: OasResult): Future[Either[ApiCataloguePublishResult, String]] = {
    logger.info(s"handleEnhancingOasForCatalogue called for ${oasResult.apiName}")
    oasParser.enhanceOas(oasResult) match {
      case Right(value: String)                   => Future.successful(Right(value))
      case Left(e: GeneralOpenApiProcessingError) =>
        logger.error(s"OpenAPI enhancements failed: ${e.message}")
        Future.successful(Left(OpenApiEnhancementFailedResult(oasResult.apiName, s"handleEnhancingOasForCatalogue failed: ${e.message}")))

    }
  }

}

case class ResultHolder(apiDefinitionResult: ApiDefinitionResult, document: WebApiDocument)

sealed trait ApiCataloguePublishResult

case class ApiDefinitionInvalidStatusResult(serviceName: String, message: String) extends ApiCataloguePublishResult
case class ApiDefinitionNotFoundResult(serviceName: String, message: String) extends ApiCataloguePublishResult
case class PublishFailedResult(serviceName: String, message: String) extends ApiCataloguePublishResult
case class OpenApiEnhancementFailedResult(serviceName: String, message: String) extends ApiCataloguePublishResult
case class ApiCataloguePublishFailedResult(serviceName: String, message: String) extends ApiCataloguePublishResult

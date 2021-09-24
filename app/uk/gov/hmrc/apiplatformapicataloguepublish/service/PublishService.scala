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

@Singleton()
class PublishService @Inject() (apiDefinitionConnector: ApiDefinitionConnector, apiRamlParser: ApiRamlParser, oasParser: OasParser)(implicit val ec: ExecutionContext) {

  def publishByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Either[ParsedResult, ConvertedWebApiToOasResult]] = {
    val result = for {
      apiDefinitionResult <- EitherT(apiDefinitionConnector.getDefinitionByServiceName(serviceName).map(mapApiDefinitionResult))
      ramlAndDefinition <- EitherT(getRamlForApiDefinition(apiDefinitionResult))
      result <- EitherT(handleRamlToOas(ramlAndDefinition))
    } yield result
    result.value
  }

  def mapApiDefinitionResult(result: Either[Throwable, ApiDefinitionResult]): Either[ParsedResult, ApiDefinitionResult] = {
    result match {
      case Left(e: Upstream4xxResponse) => Left(ApiDefinitionNotFoundResult(e.getMessage))
      case Right(x)                     => Right(x)
      case _                            => Left(PublishFailedResult("Unknown error occured"))
    }

  }

  private def getRamlForApiDefinition(apiDefinitionResult:ApiDefinitionResult): Future[Either[ParsedResult, ResultHolder]] = {
    println("*********** getRamlForApiDefinition")
    apiRamlParser.getRaml(apiDefinitionResult.url).map(x => Right(ResultHolder(apiDefinitionResult, x)))
  }

  def handleRamlToOas(ResultHolder: ResultHolder): Future[Either[ParsedResult, ConvertedWebApiToOasResult]] = {
    println("*********** handleRamlToOas")
    oasParser.parseWebApiDocument(ResultHolder.document, ResultHolder.apiDefinitionResult.serviceName, ResultHolder.apiDefinitionResult.access).map(Right(_))
  }

}

case class ResultHolder(apiDefinitionResult: ApiDefinitionResult, document: WebApiDocument)

sealed trait ParsedResult

case class ApiDefinitionNotFoundResult(message: String) extends ParsedResult
case class PublishFailedResult(message: String) extends ParsedResult

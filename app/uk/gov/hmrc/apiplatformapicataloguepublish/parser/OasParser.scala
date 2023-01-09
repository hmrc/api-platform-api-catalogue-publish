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

package uk.gov.hmrc.apiplatformapicataloguepublish.parser

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.Logging

import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.{GeneralOpenApiProcessingError, OasResult, OpenApiEnhancements, OpenApiProcessingError}
import uk.gov.hmrc.apiplatformapicataloguepublish.service.{ApiCataloguePublishResult, OpenApiEnhancementFailedResult}

@Singleton
class OasParser @Inject() (dateTimeWrapper: DateTimeWrapper)(implicit ec: ExecutionContext) extends OpenApiEnhancements with Logging {

  def handleEnhancingOasForCatalogue(oasResult: OasResult): Either[ApiCataloguePublishResult, String] = {
    logger.info(s"handleEnhancingOasForCatalogue called for ${oasResult.apiName}")
    enhanceOas(oasResult) match {
      case Right(value: String)                   => Right(value)
      case Left(e: GeneralOpenApiProcessingError) =>
        logger.error(s"OpenAPI enhancements failed: ${e.message}")
        Left(OpenApiEnhancementFailedResult(oasResult.apiName, s"handleEnhancingOasForCatalogue failed: ${e.message}"))
    }
  }

  protected[parser] def enhanceOas(convertedWebApiToOasResult: OasResult): Either[OpenApiProcessingError, String] = {
    addOasSpecAttributes(convertedWebApiToOasResult, dateTimeWrapper.generateDateNowString())
  }

}

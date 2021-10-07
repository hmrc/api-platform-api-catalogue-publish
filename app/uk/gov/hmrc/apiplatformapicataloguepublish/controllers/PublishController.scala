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

package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.service.{ApiDefinitionNotFoundResult, PublishFailedResult, PublishService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.service.ApiCataloguePublishResult
import play.api.Logging
import play.api.libs.json.{Format, Json}

@Singleton()
class PublishController @Inject() (publishService: PublishService, cc: ControllerComponents)
                                  (implicit val ec: ExecutionContext) extends BackendController(cc)
                                   with ApiCatalogueAdminJsonFormatters with Logging{

  def publish(serviceName: String): Action[AnyContent] = Action.async { implicit request =>
    //call api definition to get latest application version?(service name)

    publishService.publishByServiceName(serviceName).map {
      case Right(oasString: PublishResponse) => Ok(Json.toJson(oasString))
      case Left(e: ApiDefinitionNotFoundResult) => NotFound(s"api definition not found: ${e.message}")
      case Left(e: PublishFailedResult) => InternalServerError(s"something went wrong: ${e.message}")
      case _ =>  InternalServerError(s"something went wrong")
    }
  }

  def publishAll(): Action[AnyContent] = Action.async { implicit request =>
    publishService.publishAll().map{
      case results: List[Either[ApiCataloguePublishResult, PublishResponse]] =>
        val countSuccess = results.count(_.isRight)
        val countFailed = results.count(_.isLeft)
        results.map{
          case Right(result: PublishResponse) => logger.info(result.toString())
          case Left(e: ApiCataloguePublishResult) => logger.error(e.toString())
        }
        val response = PublishAllResponse(countSuccess, countFailed)
        Ok(Json.toJson(response))
      case _ => InternalServerError(s"something went wrong")
    }
  }

  case class PublishAllResponse(successCount: Int, failureCount: Int)
  implicit val publishAllResponseFormat: Format[PublishAllResponse] = Json.format[PublishAllResponse]
}

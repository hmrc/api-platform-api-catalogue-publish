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

package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.service.{ApiCataloguePublishResult, ApiDefinitionNotFoundResult, PublishFailedResult, PublishService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PublishController @Inject() (publishService: PublishService, cc: ControllerComponents)
                                  (implicit val ec: ExecutionContext) extends BackendController(cc)
                                   with ApiCatalogueAdminJsonFormatters with Logging{

  def publish(serviceName: String): Action[AnyContent] = Action.async { implicit request =>
    publishService.publishByServiceName(serviceName).map {
      case Right(oasString: PublishResponse) => Ok(Json.toJson(oasString))
      case Left(e: ApiDefinitionNotFoundResult) => NotFound(s"api definition not found: ${e.message}")
      case Left(e: PublishFailedResult) => InternalServerError(s"something went wrong: ${e.message}")
      case _ =>  InternalServerError(s"something went wrong")
    }
  }

  def publishAll(): Action[AnyContent] = Action.async { implicit request =>
    implicit val formatPublishAllResponse = Json.format[PublishAllResponse]
    val startTime = System.currentTimeMillis()
    logger.info("publishAll endpoint called")
    publishService.publishAll().map{
     results: List[Either[ApiCataloguePublishResult, PublishResponse]] =>
        val countSuccess = results.count(_.isRight)
        val countFailed = results.count(_.isLeft)
        results.foreach {
          case Right(result: PublishResponse) => logger.info(result.toString)
          case Left(e: ApiCataloguePublishResult) => logger.error(e.toString)
        }

      logger.info(s"publishAll about to return result -  success: $countSuccess, failed: $countFailed.took ${System.currentTimeMillis() - startTime} milliseconds ")
    }
    val publishAllResponse = PublishAllResponse(message = "Publish all called and is working in the background, check application logs for progress")
    Future.successful(Ok(Json.toJson(publishAllResponse)))
  }
}

case class PublishAllResponse(message: String)


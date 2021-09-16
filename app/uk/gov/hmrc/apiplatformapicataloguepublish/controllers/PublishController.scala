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

import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformapicataloguepublish.config.AppConfig

@Singleton()
class PublishController @Inject()(cc: ControllerComponents, appConfig: AppConfig)
    extends BackendController(cc) {

  def publish(serviceName: String): Action[AnyContent] = Action.async { implicit request =>
    //call api definition to get latest application version?(service name)
    //get raml from api producer microservice (how do we determine the link for this?)
    // convert raml to OAS and add our api catalogue specific items.
    // publish api on api catalogue
    Future.successful(Ok(s"Hello, $serviceName"))
  }
}

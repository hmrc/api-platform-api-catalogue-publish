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

import play.api.Logging
import webapi.{Raml10, WebApiDocument}

import javax.inject.{Inject, Singleton}
import scala.compat.java8._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ApiRamlParser @Inject() ()(implicit ec: ExecutionContext) extends Logging {

  def getRaml(url: String): Future[WebApiDocument] = {
    val startTime = System.currentTimeMillis()

    FutureConverters.toScala({
      Raml10.parse(url)
    }).map(x => {
      logger.info(s"getRaml - took ${System.currentTimeMillis() - startTime} milliseconds - have webapiDocument for $url")
      x.asInstanceOf[WebApiDocument]
    })
      .recover {
        case NonFatal(e) =>
          logger.error(s"getRaml - took ${System.currentTimeMillis() - startTime} milliseconds -Failed:", e)
          throw e
      }
  }

}

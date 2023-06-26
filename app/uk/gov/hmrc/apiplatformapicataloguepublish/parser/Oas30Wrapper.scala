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
import scala.concurrent.Future
import scala.jdk.FutureConverters._

import webapi.{Oas30, WebApiDocument}

import play.api.Logging

@Singleton
class Oas30Wrapper @Inject() () extends Logging {

  def ramlToOas(model: WebApiDocument): Future[String] = {
    Oas30.generateYamlString(model).asScala
  }
}

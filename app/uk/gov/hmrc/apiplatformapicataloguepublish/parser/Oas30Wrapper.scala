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

package uk.gov.hmrc.apiplatformapicataloguepublish.parser

import javax.inject.{Inject, Singleton}
import scala.compat.java8.FutureConverters
import java.util.concurrent.TimeUnit
import webapi.Oas30
import webapi.WebApiDocument
import scala.concurrent.Future

@Singleton
class Oas30Wrapper@Inject() (){

  def ramlToOas(model: WebApiDocument): Future[String]={
    FutureConverters.toScala({
      TimeUnit.MILLISECONDS.sleep(250)
      Oas30.generateYamlString(model)
    })
  }
}
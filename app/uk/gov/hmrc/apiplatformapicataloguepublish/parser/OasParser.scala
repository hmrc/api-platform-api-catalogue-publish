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

import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext}
import webapi.WebApiDocument
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiAccess
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.ConvertedWebApiToOasResult
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.PublicApiAccess
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.PrivateApiAccess

@Singleton
class OasParser @Inject() (oas30Wrapper: Oas30Wrapper)(implicit ec: ExecutionContext) extends Logging {

 def parseWebApiDocument(model: WebApiDocument, apiName: String, accessType: ApiAccess): Future[ConvertedWebApiToOasResult] = {
   oas30Wrapper.ramlToOas(model)
   .map(oasAsString => ConvertedWebApiToOasResult(oasAsString, apiName, accessTypeDescription(accessType)))
  }

  private def accessTypeDescription(accessType : ApiAccess) : String = {
    accessType match {
      case _: PublicApiAccess  => "This is a public API."
      case _: PrivateApiAccess => "This is a private API."
    }
  }
}


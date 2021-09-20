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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.service

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models.ApiDefinition
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

@Singleton()
class ApiDefinitionService @Inject() (apiDefinitionConnector: ApiDefinitionConnector)(implicit val ec: ExecutionContext) {

  def getDefinitionByServiceName(serviceName: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    apiDefinitionConnector.getDefinitionByServiceName(serviceName).map(maybeApiDefinition =>
      maybeApiDefinition
      .map(x => s"${getBaseUrl(x)}/api/conf/${getLatestVersion(x)}/application.raml")
    )
    //get raml from api producer microservice (how do we determine the link for this?)
    // convert raml to OAS and add our api catalogue specific items.
    // publish api on api catalogue
  }

  
  private def getBaseUrl(apiDefintion: ApiDefinition): String = {
    //apiDefintion.serviceBaseUrl
    s"http://localhost:9820" // customs-declarations running locally
  }

  private def getLatestVersion(apiDefintion: ApiDefinition): String = {
    //apiDefintion.versions.headOption.map(apiVersionDefinition => apiVersionDefinition.version.value).getOrElse("1.0")
    "1.0"

  }
}

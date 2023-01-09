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

package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait ApiDefinitionStub {

  def getDefinitionByNamedUrl(serviceName: String) = s"/api-definition/$serviceName"
  val getAllDefinitionsUrl = s"/api-definition?type=all"


  def primeGetByServiceName(status: Int, responseBody: String, serviceName: String): StubMapping = {
    primeGETWithBody(status, responseBody, getDefinitionByNamedUrl(serviceName))
  }

  def primeGETWithBody(status: Int, responseBody: String, urlResolver: => String): StubMapping = {
    primeWithBody(get(urlEqualTo(urlResolver)), responseBody,  status)
  }

  def primeGetAll(status: Int, responseBody: String) ={
     primeWithBody(get(getAllDefinitionsUrl), responseBody,  status)
  }


private def primeWithBody(x: MappingBuilder, responseBody: String, status: Int) = {
    stubFor(x
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody)
      )
    )
  }
}
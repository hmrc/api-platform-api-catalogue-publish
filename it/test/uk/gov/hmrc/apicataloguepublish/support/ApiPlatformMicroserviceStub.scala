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

package uk.gov.hmrc.apicataloguepublish.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ServiceName

trait ApiPlatformMicroserviceStub {

  def primeFetchApiForServiceName(status: Int, responseBody: String, serviceName: ServiceName): StubMapping = {
    stubFor(get(urlEqualTo(s"/api-definitions/service-name/$serviceName"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody)
      ))
  }

  def primeFetchApiDocumentationResource(expectedUrl: String, relativePath: String, status: Int): StubMapping = {
    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(loadFileAsByteArray(relativePath))
      ))
  }

  private def loadFileAsByteArray(relativePath: String): Array[Byte] = {
    import java.nio.file.{Files, Paths}

    val filePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + relativePath
    Files.readAllBytes(Paths.get(filePath))

  }

}

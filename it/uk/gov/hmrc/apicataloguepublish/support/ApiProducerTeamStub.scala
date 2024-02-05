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

import java.io._
import scala.language.postfixOps

import com.github.tomakehurst.wiremock.client.WireMock._

trait ApiProducerTeamStub {

  def primeGETWithFileContents(expectedUrl: String, filePath: String, status: Int) = {
    val bis    = new BufferedInputStream(new FileInputStream(filePath))
    val bArray = LazyList.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/octet-stream")
          withBody (bArray)
      ))
  }

  def primeGETReturnsNotFound(expectedUrl: String) = {

    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(404)
          withBody ("")
      ))
  }

}

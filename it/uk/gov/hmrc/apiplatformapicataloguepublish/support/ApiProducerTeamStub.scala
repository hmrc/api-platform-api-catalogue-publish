package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.WireMock._

import java.io._

trait ApiProducerTeamStub {


 def primeGETWithFileContents(expectedUrl: String, filePath: String, status: Int) = {
   val bis = new BufferedInputStream(new FileInputStream(filePath))
   val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/octet-stream")
          withBody(bArray)
      )
    )
  }

}
package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.WireMock._

trait ApiProducerTeamStub {


 def primeGETWithFileContents(expecterdUrl: String, filePath: String, status: Int) = {
  
    stubFor(get(urlEqualTo(expecterdUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/octet-stream")
          .withHeader("Content-Length", "19910")
          withBodyFile(filePath)
      )
    )
  }

}
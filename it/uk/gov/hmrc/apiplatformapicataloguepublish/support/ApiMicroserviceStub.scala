package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait ApiMicroserviceStub {

  def primeFetchResource(url: String, relativePath: String, status: Int): StubMapping = {
    primeGETWithBody(url, loadFileAsByteArray(relativePath),status)
  }

 def primeGETWithBody(expectedUrl: String, body: Array[Byte], status: Int): StubMapping = {

    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(body)
      )
    )
  }

  private def loadFileAsByteArray(relativePath: String): Array[Byte] = {
    import java.nio.file.{Files, Paths}

    val filePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + relativePath
    Files.readAllBytes(Paths.get(filePath))

  }

}
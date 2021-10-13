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

  def primeGETWithFileContentsAndRandomDelay(expectedUrl: String, filePath: String, delay: Int) = {
    val bis = new BufferedInputStream(new FileInputStream(filePath))
    val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
   println(s"primeGETWithFileContentsAndRandomDelay: about to stub $expectedUrl")
    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/octet-stream")
          .withBody(bArray)
          .withChunkedDribbleDelay(5, delay)

      )
    )
  }

  def primeGETReturnsError(expectedUrl: String, status: Int = 404) = {

    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          withBody("")
      )
    )
  }

  def primeGETReturnsErrorWithDelay(expectedUrl: String, status: Int = 404) = {
    println(s"primeGETReturnsErrorWithDelay: about to stub $expectedUrl")
    stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody("")
          .withFixedDelay(5000)
      )
    )
  }

}
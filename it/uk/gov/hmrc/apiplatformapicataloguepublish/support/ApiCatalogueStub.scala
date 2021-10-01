package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait ApiCatalogueStub {

  def primeApiPublish(body: String, status: Int): StubMapping = {
    primePOSTWithBody("/integration-catalogue-admin-api/services/apis/publish", body, status)
  }

 def primePOSTWithBody(expectedUrl: String, body: String, status: Int): StubMapping = {

    stubFor(put(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json")
          .withBody(body)
      )
    )
  }

}
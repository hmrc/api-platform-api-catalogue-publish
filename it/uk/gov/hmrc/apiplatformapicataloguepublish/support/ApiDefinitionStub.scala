package uk.gov.hmrc.apiplatformapicataloguepublish.support

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait ApiDefinitionStub {

  def getDefinitionByNamedUrl(serviceName: String) = s"/api-definition/$serviceName"


  def primeGetByServiceName(status: Int, responseBody: String, serviceName: String): StubMapping = {
    primeGETWithBody(status, responseBody, getDefinitionByNamedUrl(serviceName))
  }

  def primeGETWithBody(status: Int, responseBody: String, urlResolver: => String): StubMapping = {
    primeWithBody(get(urlEqualTo(urlResolver)), responseBody,  status)
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
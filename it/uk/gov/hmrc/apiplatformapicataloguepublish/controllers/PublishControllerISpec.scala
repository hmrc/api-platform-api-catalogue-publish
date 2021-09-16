package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{BAD_REQUEST, NOT_FOUND, OK}

import uk.gov.hmrc.apiplatformapicataloguepublish.support.{AwaitTestSupport, ServerBaseISpec}
import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.duration._

class PublishControllerISpec extends ServerBaseISpec  with AwaitTestSupport {


  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> true,
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.host" -> wireMockHost,
        "auditing.consumer.baseUri.port" -> wireMockPort
      )

        val url = s"http://localhost:$port/api-platform-api-catalogue-publish/publish"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val validHeaders = List(CONTENT_TYPE -> "application/json")

  def callPostEndpoint(url: String, body: String, headers: List[(String, String)]): WSResponse =
    wsClient
      .url(url)
      .withHttpHeaders(headers: _*)
      .withFollowRedirects(false)
      .post(body)
      .futureValue

  trait Setup {

      def callPublishEndpoint(serviceName: String)={
        callPostEndpoint(s"$url/$serviceName", body= "", List.empty)
      }
  }

  "PublishController" when {

    "POST /publish/[serviceName]" should {
      "respond with 200 " in new Setup {

        val result: WSResponse = callPublishEndpoint("hello")
        result.status shouldBe OK
        
      }

  

   
    }

   
    


  }


}

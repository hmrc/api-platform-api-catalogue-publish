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

package uk.gov.hmrc.apiplatformapicataloguepublish.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PlatformType.API_PLATFORM
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, IntegrationId, PublishResponse}
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._
import uk.gov.hmrc.apiplatformapicataloguepublish.data.ApiDefinitionData
import uk.gov.hmrc.apiplatformapicataloguepublish.support._
import webapi.{Raml10, WebApiDocument}

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.io.Source
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.utils.ApiDefinitionUtils

import java.util.UUID

class PublishControllerISpec
    extends ServerBaseISpec
    with AwaitTestSupport
    with BeforeAndAfterEach
    with MetricsTestSupport
    with ApiDefinitionStub
    with ApiProducerTeamStub
    with ApiDefinitionData
    with ApiDefinitionJsonFormatters
    with ApiDefinitionUtils
    with ApiCatalogueStub
    with ApiCatalogueAdminJsonFormatters {

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> true,
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.host" -> wireMockHost,
        "auditing.consumer.baseUri.port" -> wireMockPort,
        "microservice.services.api-definition.host" -> wireMockHost,
        "microservice.services.api-definition.port" -> wireMockPort,
        "microservice.services.integration-catalogue-admin-api.host" -> wireMockHost,
        "microservice.services.integration-catalogue-admin-api.port" -> wireMockPort
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCleanMetricRegistry()

  }

  val url = s"http://localhost:$port/api-platform-api-catalogue-publish"

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

    def callPublishEndpoint(serviceName: String) = {
      callPostEndpoint(s"$url/publish/$serviceName", body = "", List.empty)
    }

    def callPublishAllEndpoint() = {
      callPostEndpoint(s"$url/publish-all", body = "", List.empty)
    }

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def absoluteRamlFilePath = Paths.get(".").toAbsolutePath.toString.replace(".", "") + "it/resources/test-ramlFile.raml"

    def getRamlUri(apiDefinition: ApiDefinition) ={
      getUri(apiDefinition) + ".raml"
    }

  }

  "PublishController" when {

    "POST /publish/[serviceName]" should {
      "respond with 200 when publish successful" in new Setup {
        val serviceName = "my-service"
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(apiDefinition1withwiremock).toString
        val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
        val publishResponseAsJsonString: String = Json.toJson(publishResponse).toString

        primeGetByServiceName(OK, apiDefinitionAsString, serviceName)
        primeGETWithFileContents("/" + getRamlUri(apiDefinition1), absoluteRamlFilePath, OK)
        primeApiPublish(publishResponseAsJsonString, OK)

        val result: WSResponse = callPublishEndpoint(serviceName)
        result.status mustBe OK
      }

      "respond with 404 when api definition not found" in new Setup {
        val serviceName = "my-service"

        primeGetByServiceName(NOT_FOUND, "{}}", serviceName)

        val result: WSResponse = callPublishEndpoint(serviceName)
        result.status mustBe NOT_FOUND
      }

      "respond with 500 when getRaml fails" in new Setup {
        val serviceName = "my-service"
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(apiDefinition1withwiremock).toString

        primeGetByServiceName(OK, apiDefinitionAsString, serviceName)
        primeGETReturnsNotFound("/" + getRamlUri(apiDefinition1))

        val result: WSResponse = callPublishEndpoint(serviceName)
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "respond with 500 when publish fails" in new Setup {
        val serviceName = "my-service"
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(apiDefinition1withwiremock).toString
        val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
        val publishResponseAsJsonString: String = Json.toJson(publishResponse).toString

        primeGetByServiceName(OK, apiDefinitionAsString, serviceName)
        primeGETWithFileContents("/" + getRamlUri(apiDefinition1), absoluteRamlFilePath, OK)
        primeApiPublish(publishResponseAsJsonString, BAD_REQUEST)

        val result: WSResponse = callPublishEndpoint(serviceName)
        result.status mustBe INTERNAL_SERVER_ERROR
      }

    }

    "POST /publish-all" should {
      "respond with 200 when get all definitions fails" in new Setup {
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(List(apiDefinition1withwiremock)).toString

        primeGetAll(NOT_FOUND, apiDefinitionAsString)

        val result: WSResponse = callPublishAllEndpoint()
        result.status mustBe OK
        result.body mustBe """{"message":"Publish all called and is working in the background, check application logs for progress"}"""
      }

      "respond with 200 when publish fails" in new Setup {
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(List(apiDefinition1withwiremock)).toString
        val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
        val publishResponseAsJsonString: String = Json.toJson(publishResponse).toString

        primeGetAll(OK, apiDefinitionAsString)
        primeGETWithFileContents("/" + getRamlUri(apiDefinition1), absoluteRamlFilePath, OK)
        primeApiPublish(publishResponseAsJsonString, BAD_REQUEST)

        val result: WSResponse = callPublishAllEndpoint()
        result.status mustBe OK
        result.body mustBe """{"message":"Publish all called and is working in the background, check application logs for progress"}"""
      }

      "respond with 200 when publish is successful" in new Setup {
        val apiDefinition1withwiremock = apiDefinition1.copy(serviceBaseUrl = s"http://$wireMockHost:$wireMockPort/${apiDefinition1.serviceBaseUrl}")
        val apiDefinitionAsString = Json.toJson(List(apiDefinition1withwiremock)).toString
        val publishResponse: PublishResponse = PublishResponse(IntegrationId(UUID.randomUUID()), "somePublisherRef", API_PLATFORM)
        val publishResponseAsJsonString: String = Json.toJson(publishResponse).toString

        primeGetAll(OK, apiDefinitionAsString)
        primeGETWithFileContents("/" + getRamlUri(apiDefinition1), absoluteRamlFilePath, OK)
        primeApiPublish(publishResponseAsJsonString, OK)

        val result: WSResponse = callPublishAllEndpoint()
        result.status mustBe OK
        result.body mustBe """{"message":"Publish all called and is working in the background, check application logs for progress"}"""
      }
    }
  }
}

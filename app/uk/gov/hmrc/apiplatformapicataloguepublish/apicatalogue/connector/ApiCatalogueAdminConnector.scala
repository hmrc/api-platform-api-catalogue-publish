/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector.Config

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.http.ws.WSPut

import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PublishResult
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PublishDetails
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.PublishError
import uk.gov.hmrc.http.HeaderCarrier
import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, ListenableFuture, RequestBuilder, Response}
import play.shaded.ahc.org.asynchttpclient.request.body.multipart.{ByteArrayPart, StringPart}

import java.nio.charset.Charset
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.libs.json.JsValue

import java.util.UUID
import akka.stream.scaladsl.Source
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData
import play.libs.Files.{TemporaryFile, TemporaryFileCreator}


@Singleton
class ApiCatalogueAdminConnector @Inject() (
    val ws: WSClient,
    val fileCreator: Files.TemporaryFileCreator
  )(implicit val ec: ExecutionContext)
    extends Logging {

  private def publishApiUrl(serviceBaseUrl: String) = s"$serviceBaseUrl/integration-catalogue-admin-api/services/apis/publish"
  def createTempFile(fileName:String, content: String)={
      val file = fileCreator.create()


  }
  def publishApi(body: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
       implicit val publishErrorReads = Json.format[PublishError]
       implicit val publishdetailsReads = Json.format[PublishDetails]
       implicit val publishResultReads = Json.format[PublishResult]

      val headers = Map.empty

      ws.url(publishApiUrl("http://localhost:11114"))
        .withHttpHeaders(("x-platform-type"-> "API_PLATFORM"),
          ("AUTHORIZATION" -> "dGVzdC1hdXRoLWtleQ=="),
          ("x-specification-type" -> "OAS_V3"),
          ("ContentType" -> "multipart/form-data"))
      .put(Source.single(MultipartFormData.DataPart("selectedFile", body)))

                   .map(response => {
                    logger.debug(response.body)
                Json.parse(response.body)}
                )

    // ws.url(publishApiUrl("http://localhost:11114"))
    //             .withHttpHeaders(("AUTHORISATION" -> "dGVzdC1hdXRoLWtleQ=="),
    //             ("x-platform-type" -> "API_PLATFORM"),
    //             ("x-specification-type" -> "OAS_V3"),
    //             ("Content-Type" -> "multipart/form-data; boundary=---------------------------293582696224464"))
                
    //             .withBody(body.getBytes())
    //             .execute("PUT")
    //             .map(response => {
    //                 logger.debug(response.body)
    //             Json.parse(response.body)}
    //             )
    
  }


  def getRequestBuilder(url: String, fileMetadata: Map[String, String], headers: Map[String, String],name:String, fileName:String, fileContent: Array[Byte],mimeType:String) = {


    val rb = new RequestBuilder("POST").setUrl(url)
     fileMetadata.foreach {
      case (key, value) =>
        rb.addBodyPart(new StringPart(key, value))
    }

    val byteParts = new ByteArrayPart(name,fileContent,mimeType,Charset.forName("UTF-8"),fileName)
    rb.addBodyPart((byteParts))

    headers.foreach {
      case (key, value) =>
        if (addHeader(key)) {
          rb.addHeader(key, value)
        }
    }

    rb
  }

     def addHeader(headerName: String): Boolean = !"Content-Type".equals(headerName) && !"Content-Length".equals(headerName)
}

object ApiCatalogueAdminConnector {
  case class Config(baseUrl: String)


}

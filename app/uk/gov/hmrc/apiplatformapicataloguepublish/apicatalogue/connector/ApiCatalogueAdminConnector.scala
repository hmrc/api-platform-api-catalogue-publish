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

package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import akka.stream.scaladsl.Source
import play.api.Logging
import play.api.libs.Files
import play.api.libs.json.{JsError, JsResult, JsSuccess}
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector._
import uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.models.{ApiCatalogueAdminJsonFormatters, PublishResponse}

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiCatalogueAdminConnector @Inject()(val ws: WSClient,
                                            val config: Config,
                                            val fileCreator: Files.TemporaryFileCreator
                                          )(implicit val ec: ExecutionContext)
  extends Logging with ApiCatalogueAdminJsonFormatters {

  def publishApi(body: String): Future[Either[ApiCatalogueFailedResult, PublishResponse]] = {
    val startTime = System.currentTimeMillis()
    logger.info(s"publishApi called")
    val authKey = new String(Base64.getEncoder.encode(config.authorizationKey.getBytes))

    val headers = Seq("x-platform-type" -> "API_PLATFORM",
      "AUTHORIZATION" -> authKey,
      "x-specification-type" -> "OAS_V3",
      "ContentType" -> "multipart/form-data")

    ws.url(s"${config.baseUrl}/integration-catalogue-admin-api/services/apis/publish")
      .withHttpHeaders(headers: _*)
      .put(Source.single(MultipartFormData.DataPart("selectedFile", body)))
      .map(response => response.status match {
          case s: Int if(s ==200 || s==201) =>  
            logger.info(s"publishApi successful and took ${System.currentTimeMillis() - startTime} milliseconds")
            handleJsResult(response.json.validate[PublishResponse])
          case _ => 
            logger.info(s"publishApi failed and took ${System.currentTimeMillis() - startTime} milliseconds")
            Left(ApiCatalogueGeneralFailureResult("Publish failed"))
        })
  }

  def handleJsResult( result : JsResult[PublishResponse]): Either[ApiCatalogueFailedResult, PublishResponse] ={
      result match {
        case s: JsSuccess[PublishResponse] => Right(s.get)
        case e: JsError => logger.error(s"Js Parse Errors:  ${JsError.toJson(e).toString()}")
        Left(ApiCatalogueGeneralFailureResult(s"Js Parse Errors:  ${JsError.toJson(e).toString()}"))
      }
  }
}

object ApiCatalogueAdminConnector {
  case class Config(baseUrl: String, authorizationKey: String)


  sealed trait ApiCatalogueFailedResult {
    val message: String
  }
  case class ApiCatalogueGeneralFailureResult(message: String) extends  ApiCatalogueFailedResult
}

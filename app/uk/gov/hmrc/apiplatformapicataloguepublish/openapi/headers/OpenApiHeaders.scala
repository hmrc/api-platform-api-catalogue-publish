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

package uk.gov.hmrc.apiplatformapicataloguepublish.openapi.headers

import java.util
import scala.collection.JavaConverters._

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem}

import uk.gov.hmrc.apiplatformapicataloguepublish.openapi.ExtensionKeys

trait OpenApiHeaders extends ExtensionKeys {

  private def addHeader(name: String, description: String, operation: Operation): Unit = {
    val stringSchema = new Schema()
    stringSchema.setType("string")

    val contentTypeHeader = new Parameter()
    contentTypeHeader.setIn("header")
    contentTypeHeader.setName(name)
    contentTypeHeader.setDescription(description)
    contentTypeHeader.setSchema(stringSchema)
    contentTypeHeader.setRequired(true)
    operation.addParametersItem(contentTypeHeader)
  }

  def addContentTypeHeader(operation: Operation): Unit = {
    addHeader("Content-Type", "Specifies the format of the request body, which must be JSON. For example: `application/json`", operation)
  }

  def addAcceptHeader(operation: Operation): Unit = {
    addHeader("Accept", "Specifies the response format and the version of the API to be used. For example: `application/vnd.hmrc.1.0+json`", operation)
  }

  def addAuthorizationHeader(operation: Operation): Unit = {
    addHeader("Authorization", "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`", operation)
  }

  def handleTopLevelHeaders(pathItem: PathItem): Option[List[String]] = {
    Option(pathItem.getExtensions).map(x =>
      Option(x.get(X_AMF_IS)) match {
        case Some(values: util.List[String]) =>
          val amfIs   = values.asScala.toList
          val newList = if (amfIs.contains(ACCEPT_HEADER)) List(ACCEPT_HEADER) else List.empty[String]
          if (amfIs.contains(CONTENTTYPE_HEADER)) newList ++ List(CONTENTTYPE_HEADER) else newList
        case _                               => List.empty[String]
      }
    )
  }

  def addOperationLevelHeaders(openAPI: OpenAPI): OpenAPI = {

    def handlePathItem(pathItem: PathItem): OpenAPI = {
      def handleOperationsMap(operationsMap: util.Map[HttpMethod, Operation], topLevelHeaders: List[String]): OpenAPI = {
        operationsMap.values.asScala.foreach { operation =>
          {
            if (topLevelHeaders.contains(ACCEPT_HEADER)) addAcceptHeader(operation)
            if (topLevelHeaders.contains(CONTENTTYPE_HEADER)) addContentTypeHeader(operation)
            Option(operation.getSecurity)
              .map(_.asScala.map(sec => if (sec.keySet().contains(SEC_O_AUTH) || sec.keySet().contains(SEC_APPLICATION)) addAuthorizationHeader(operation)))
            Option(operation.getExtensions).map(x =>
              Option(x.get(X_AMF_IS)) match {
                case Some(values: util.List[String]) => {
                  val amfIs = values.asScala.toList
                  if (amfIs.contains(ACCEPT_HEADER) && !topLevelHeaders.contains(ACCEPT_HEADER)) addAcceptHeader(operation)
                  if (amfIs.contains(CONTENTTYPE_HEADER) && !topLevelHeaders.contains(CONTENTTYPE_HEADER)) addContentTypeHeader(operation)
                }
                case _                               => ()
              }
            )
          }

        }
        openAPI
      }
      handleOperationsMap(pathItem.readOperationsMap(), handleTopLevelHeaders(pathItem).getOrElse(List.empty))

    }

    Option(openAPI.getPaths).map(paths => paths.asScala.toMap.values.map(handlePathItem))
    openAPI
  }

}

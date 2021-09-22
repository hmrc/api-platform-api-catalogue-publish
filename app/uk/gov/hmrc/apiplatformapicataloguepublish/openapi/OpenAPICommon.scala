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

package uk.gov.hmrc.apiplatformapicataloguepublish.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter

import java.util
import scala.collection.JavaConverters._
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.PathItem

trait OpenAPICommon extends ExtensionKeys {


 private  def addHeader(name: String, description: String, operation: Operation): Unit ={
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

      def addContentTypeHeader(operation: Operation) ={
      addHeader("Content-Type", "Specifies the format of the request body, which must be JSON. For example: `application/json`", operation)
    }
    def addAcceptHeader(operation: Operation) ={
      addHeader("Accept", "Specifies the response format and the version of the API to be used. For example: `application/vnd.hmrc.1.0+json`", operation)
    }

   def addOperationLevelHeaders(openAPI: OpenAPI)={

    def handlePathItem(pathItem: PathItem)={
      
      handleOperationsMap(pathItem.readOperationsMap(), handleTopLevelHeaders(pathItem).getOrElse(List.empty))
      
    }

    def handleTopLevelHeaders(pathItem: PathItem)={
      Option(pathItem.getExtensions()).map(x => Option(x.get(X_AMF_IS)) match {
              case Some(values: util.List[String]) => {
                  val amfIs = values.asScala.toList
               val newList =   if(amfIs.contains(ACCEPT_HEADER))  List(ACCEPT_HEADER) else List.empty[String]
               if(amfIs.contains(CONTENTTYPE_HEADER)) newList ++ List(CONTENTTYPE_HEADER) else newList
              }
              case _ => List.empty[String]
            })
    }
    
    def handleOperationsMap(operationsMap: util.Map[HttpMethod, Operation], topLevelHeaders: List[String]) ={
     
      operationsMap.values.asScala.foreach {operation => {
          if(topLevelHeaders.contains(ACCEPT_HEADER)) addAcceptHeader(operation)
          if(topLevelHeaders.contains(CONTENTTYPE_HEADER)) addContentTypeHeader(operation)
            Option(operation.getSecurity()).map(_.asScala.map(sec => if( sec.keySet().contains(SEC_O_AUTH) || sec.keySet().contains(SEC_APPLICATION)) addAuthorizationHeader(operation)))
            Option(operation.getExtensions()).map(x => Option(x.get(X_AMF_IS)) match {
              case Some(values: util.List[String]) => {
                  val amfIs = values.asScala.toList
                  if(amfIs.contains(ACCEPT_HEADER) && !topLevelHeaders.contains(ACCEPT_HEADER)) addAcceptHeader(operation)
                  if(amfIs.contains(CONTENTTYPE_HEADER) && !topLevelHeaders.contains(CONTENTTYPE_HEADER)) addContentTypeHeader(operation)
              }
              case _ => ()
            })
          }

      }
      openAPI
    }

    Option(openAPI.getPaths)
      .map(paths => paths.asScala.toMap.values.map(handlePathItem))

    openAPI
  }

  def addAuthorizationHeader(operation: Operation) = {
      addHeader("Authorization","An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`", operation) 
  }


  def extractDocumentation(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]]): List[SubDocument] = {
    val convertedList = extensionData.asScala.toList
    convertedList.flatMap(x => {
      val maybeContent = Option(x.get("content"))
      val mayBeTitle = Option(x.get("title"))
      (mayBeTitle, maybeContent) match {
        case (Some(title: String), Some(content: String)) => Some(SubDocument(apiName, title, content))
        case _                                            => None
      }
    })
  }

  private def getExtensions(openApi: OpenAPI, key: String): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    Option(openApi.getExtensions()).flatMap(extensionsMap =>
      Option(extensionsMap.get(key))
        .map(x => {
          x match {
            case y : util.ArrayList[java.util.LinkedHashMap[String, Object]] => y
            case z : java.util.LinkedHashMap[String, Object]                  => {
              val list = new util.ArrayList[java.util.LinkedHashMap[String, Object]]()
              list.add(z)
              list
            }
          }

        })
    )
  }

  def getXamfDocumentationExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    getExtensions(openApi, X_AMF_USERDOCUMENTATION_KEY)
  }

}

/*
 * Copyright 2022 HM Revenue & Customs
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

import java.util
import scala.collection.JavaConverters._

trait OpenAPICommon extends ExtensionKeys {


  def extractDocumentation(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]]): List[SubDocument] = {
    val convertedList = extensionData.asScala.toList
    convertedList.flatMap(x => {
      val maybeContent = Option(x.get("content"))
      val mayBeTitle = Option(x.get("title"))
      (mayBeTitle, maybeContent) match {
        case (Some(title: String), Some(content: String)) if(!isUrl(content)) => Some(SubDocument(apiName, title, content))
        case _ => None
      }
    })
  }

  def isUrl(content: String): Boolean = {
    content.startsWith("http")
}

  private def getExtensions(openApi: OpenAPI, key: String): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    Option(openApi.getExtensions).flatMap(extensionsMap =>
      Option(extensionsMap.get(key))
        .map {
          case y: util.ArrayList[java.util.LinkedHashMap[String, Object]] => y
          case z: java.util.LinkedHashMap[String, Object] => {
            val list = new util.ArrayList[java.util.LinkedHashMap[String, Object]]()
            list.add(z)
            list
          }
        }
    )
  }

  def getXamfDocumentationExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    getExtensions(openApi, X_AMF_USERDOCUMENTATION_KEY)
  }

}

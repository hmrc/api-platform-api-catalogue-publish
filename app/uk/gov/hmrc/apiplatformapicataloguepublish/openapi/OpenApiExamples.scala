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

package uk.gov.hmrc.apiplatformapicataloguepublish.openapi

import scala.collection.JavaConverters._

import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.{OpenAPI, Operation}

trait OpenApiExamples extends ExtensionKeys {

  def handleContent(content: Content): Unit = {

    def handleLinkHashMap(a: java.util.LinkedHashMap[String, Object], mt: MediaType, exampleKey: String): MediaType = {
      val example = new Example()
      a.asScala.get("description").map(_.toString).foreach(example.setDescription)
      a.asScala.get("value").fold(example.setValue(a))(x => example.setValue(x))
      mt.addExamples(exampleKey, example)
    }

    content.values().asScala.map(mt => {
      val examples = Option(mt.getSchema).flatMap(x => Option(x.getExtensions).flatMap(extensions => Option(extensions.get(X_AMF_EXAMPLES))))
      examples match {
        case Some(z: java.util.LinkedHashMap[String, Object]) => z.asScala.map(x => {
            x._2 match {
              case a: java.util.LinkedHashMap[String, Object] => handleLinkHashMap(a, mt, x._1)
              case _                                          => ()
            }
          })
        case _                                                => ()
      }
    })
    ()
  }

  def handleOperation(operation: Operation): Option[Unit] = {
    Option(operation.getRequestBody)
      .map(request => Option(request.getContent).map(handleContent))

    Option(operation.getResponses)
      .map(responses =>
        responses.values
          .forEach(response => Option(response.getContent).map(handleContent))
      )
  }

  def addExamples(openAPI: OpenAPI): OpenAPI = {
    Option(openAPI.getPaths)
      .map(paths =>
        paths.values
          .forEach(pathItem => pathItem.readOperations().asScala.map(handleOperation))
      )
    openAPI
  }
}

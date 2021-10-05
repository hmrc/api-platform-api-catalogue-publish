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
import java.util

trait ValidateXamfText extends OpenAPICommon {

  def validateAmfOAS(openApi: OpenAPI, apiName: String): Either[OpenApiProcessingError, OpenAPI] = {

    def validateSubdocuments(subdocuments: List[SubDocument], openApi: OpenAPI): Either[GeneralOpenApiProcessingError, OpenAPI] = {
      val errorListAsStrings: List[String] = subdocuments.filter(document => {
          document.content.contains("https://developer.service.hmrc.gov.uk/api-documentation/assets/")
      })
        .map(document => s"API: ${document.apiName} content for title: ${document.title} points to a file!! ${document.content} \n")

      if (errorListAsStrings.nonEmpty) {
        Left(GeneralOpenApiProcessingError(apiName, errorListAsStrings.mkString))
      } else Right(openApi)

    }

    val maybeUserDocumentationExtensions: Option[util.ArrayList[util.LinkedHashMap[String, AnyRef]]] =
      Option(openApi).flatMap(getXamfDocumentationExtensions)

    val listOfSubDocuments: List[SubDocument] =
      maybeUserDocumentationExtensions.map(x => extractDocumentation(apiName, x)).getOrElse(List.empty)

    validateSubdocuments(listOfSubDocuments, openApi)

  }

}

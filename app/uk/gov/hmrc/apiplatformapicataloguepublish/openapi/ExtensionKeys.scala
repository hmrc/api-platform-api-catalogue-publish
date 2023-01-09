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

trait ExtensionKeys {
  val EXTENSIONS_KEY: String = "x-integration-catalogue"
  val BACKEND_EXTENSION_KEY: String = "backends"
  val PUBLISHER_REF_EXTENSION_KEY: String = "publisher-reference"
  val PLATFORM_EXTENSION_KEY: String = "platform"
  val SHORT_DESC_EXTENSION_KEY: String = "short-description"
  val REVIEWED_DATE_EXTENSION_KEY: String = "reviewed-date"
  val X_AMF_USERDOCUMENTATION_KEY: String = "x-amf-userDocumentation"
  val X_AMF_TITLE_KEY: String = "x-amf-title"
  val X_AMF_USES: String = "x-amf-uses"
  val X_AMF_IS: String = "x-amf-is"
  val SEC_O_AUTH: String = "sec.oauth_2_0"
  val SEC_APPLICATION: String = "sec.x-application"
  val ACCEPT_HEADER: String = "headers.acceptHeader"
  val CONTENTTYPE_HEADER: String = "headers.contentHeader"
  val X_AMF_EXAMPLES: String = "x-amf-examples"
}


case class SubDocument(apiName: String, title: String, content: String)

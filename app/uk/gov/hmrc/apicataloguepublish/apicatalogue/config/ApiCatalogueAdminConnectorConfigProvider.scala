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

package uk.gov.hmrc.apicataloguepublish.apicatalogue.config

import javax.inject.{Inject, Singleton}

import com.google.inject.Provider

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.apicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector

@Singleton
class ApiCatalogueAdminConnectorConfigProvider @Inject() (sc: ServicesConfig) extends Provider[ApiCatalogueAdminConnector.Config] {

  override def get(): ApiCatalogueAdminConnector.Config = {
    lazy val baseUrl          = sc.baseUrl("integration-catalogue-admin-api")
    lazy val authorizationKey = sc.getString("publish.authKey.apiPlatform")
    ApiCatalogueAdminConnector.Config(baseUrl, authorizationKey)
  }
}

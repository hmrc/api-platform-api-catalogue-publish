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

package uk.gov.hmrc.apicataloguepublish.config

import com.google.inject.AbstractModule

import uk.gov.hmrc.apicataloguepublish.apicatalogue.config.ApiCatalogueAdminConnectorConfigProvider
import uk.gov.hmrc.apicataloguepublish.apicatalogue.connector.ApiCatalogueAdminConnector
import uk.gov.hmrc.apicataloguepublish.apidefinition.config.ApiDefinitionConnectorConfigProvider
import uk.gov.hmrc.apicataloguepublish.apidefinition.connector.ApiDefinitionConnector

class ConfigurationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[ApiCatalogueAdminConnector.Config])
      .toProvider(classOf[ApiCatalogueAdminConnectorConfigProvider])
    bind(classOf[ApiDefinitionConnector.Config])
      .toProvider(classOf[ApiDefinitionConnectorConfigProvider])
  }

}

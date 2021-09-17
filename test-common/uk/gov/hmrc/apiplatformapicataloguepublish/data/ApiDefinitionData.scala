package uk.gov.hmrc.apiplatformapicataloguepublish.data

import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models._

trait ApiDefinitionData {
      val categories = List(ApiCategory("category1"), ApiCategory("category2"))
    val serviceName = "my-service"
    val versions = List(apiVersion(version = ApiVersion.random), apiVersion(version = (ApiVersion("2.0"))))

    val apiDefinition1 = ApiDefinition(serviceName, s"$serviceName-name", s"$serviceName-description", ApiContext.random, false, false, versions.toList, categories)
}

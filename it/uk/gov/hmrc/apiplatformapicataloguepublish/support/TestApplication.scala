package uk.gov.hmrc.apiplatformapicataloguepublish.support


import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait TestApplication {
  _: BaseISpec =>

  override implicit lazy val app: Application = appBuilder.build()

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                 -> true,
        "auditing.enabled"                -> true,
        "auditing.consumer.baseUri.host"  -> wireMockHost,
        "auditing.consumer.baseUri.port"  -> wireMockPort
      )

}


package uk.gov.hmrc.apiplatformapicataloguepublish.apicatalogue.connector

import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiMicroserviceConnector @Inject()(ws: WSClient)(implicit val ec: ExecutionContext) extends Logging {

  def fetchApiDocumentationResourceByUrl(url: String): Future[Either[Throwable, String]] = {
    logger.info(s"Calling local microservice to fetch resource by URL: $url")
    ws.url(url).withMethod("GET").stream().map {
      streamedResponse =>

      streamedResponse.status match {
        case OK => Right(streamedResponse.body)
        case NOT_FOUND => Left(new NotFoundException(s"Resource not found - $url"))
        case _ => Left(new InternalServerException(s"Error downloading resource - $url"))
      }
    }

  }
}

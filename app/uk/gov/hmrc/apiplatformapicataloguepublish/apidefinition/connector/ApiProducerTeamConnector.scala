package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector

import play.api.Logging
import uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.connector.ApiDefinitionConnector.Config
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import uk.gov.hmrc.play.http.ws.WSGet

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
@Singleton
class ApiProducerTeamConnector @Inject()(val http: HttpClient with WSGet,
                                        val config: Config)(implicit ec: ExecutionContext) extends Logging{


  def getApiRamlString(hostName: String, path: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, String]] = {
    logger.info(s"${this.getClass.getSimpleName} - getApiRaml")
    val r = http.GET[Option[String]](hostName + path).map {
        case Some(value) => Right(value)
        case _ => Left(new NotFoundException("unable to retrieve RAML"))
      }

    r.recover {
      case NonFatal(e)          =>
        logger.error(s"Failed $e")
        Left(e)
    }
  }

}

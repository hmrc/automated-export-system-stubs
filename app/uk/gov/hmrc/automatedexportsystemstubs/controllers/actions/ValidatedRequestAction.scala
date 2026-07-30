package uk.gov.hmrc.automatedexportsystemstubs.controllers.actions

import play.api.http.HeaderNames
import play.api.mvc.Results.{BadRequest, NoContent}
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, BodyParsers, Request, Result, WrappedRequest}
import uk.gov.hmrc.automatedexportsystemstubs.config.AppConfig
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.request.ValidatedRequest

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class ValidatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class ValidatedRequestAction @Inject()(
                                        val parser: BodyParsers.Default,
                                        appConfig: AppConfig
                                      )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, ValidatedRequest]
    with ActionBuilder[ValidatedRequest, AnyContent] {
  
  private def currentHttpDate: String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())
  
  private def buildResponse(result: Result, incomingRequest: Request[?]): Result = {
    val withDate = result.withHeaders(HeaderNames.DATE -> currentHttpDate)

    incomingRequest.headers.get("x-correlation-id") match {
      case Some(id) => withDate.withHeaders("x-correlation-id" -> id)
      case None     => withDate
    }
  }

  override protected def refine[A](request: Request[A]): Future[Either[Result, ValidatedRequest[A]]] = Future.successful {

    val invalidHeaders = appConfig.requiredHeaders.filter { case (key, expectedValue) =>
      request.headers.get(key) match {
        case None =>
          true 
        case Some(actualValue) if expectedValue != "*" && actualValue != expectedValue =>
          true
        case _ =>
          false 
      }
    }

    if (invalidHeaders.nonEmpty) {
      val errorMessage = s"Missing or invalid mandatory headers: ${invalidHeaders.keys.mkString(", ")}" //TODO: fix the error message
      Left(buildResponse(BadRequest(errorMessage), request))
    } else {
      Right(ValidatedRequest(request))
    }
  }
  
  def successResponse(request: Request[?]): Result = {
    buildResponse(NoContent, request)
  }
}
/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.automatedexportsystemstubs.controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.utils.ErrorResponseHelper

import javax.inject.{Inject, Singleton}

@Singleton()
class MessageController @Inject() (
  cc:              ControllerComponents,
  validatedAction: ValidatedRequestAction
) extends AbstractController(cc):

  def message(): Action[AnyContent] = validatedAction { implicit request =>
    val correlationId = request.headers.get("x-correlation-id").getOrElse("")
    request.body.asXml match
      case Some(xml) =>
        val mrn = (xml \\ "MRN").headOption.map(_.text.trim)

        mrn match
          case Some(value) if value.endsWith("A0") =>
            validatedAction.errorResponse(
              ErrorResponseHelper.createErrorResponse(
                status = 401,
                correlationId,
                errorMessage = "UNAUTHORIZED",
                detail = "Invalid or missing token"
              ),
              request
            )

          case Some(value) if value.endsWith("A1") =>
            validatedAction.errorResponse(
              ErrorResponseHelper.createErrorResponse(
                status = 404,
                correlationId,
                errorMessage = "NOT_FOUND",
                detail = "EIS endpoint not found"
              ),
              request
            )

          case Some(value) if value.endsWith("A2") =>
            validatedAction.errorResponse(
              ErrorResponseHelper.createErrorResponse(
                status = 500,
                correlationId,
                errorMessage = "INTERNAL_SERVER_ERROR",
                detail = "Server error"
              ),
              request
            )

          case Some(value) if value.endsWith("A3") =>
            validatedAction.errorResponse(
              ErrorResponseHelper.createErrorResponse(
                status = 400,
                correlationId,
                errorMessage = "VALIDATION_ERROR",
                detail = "Validation error"
              ),
              request
            )

          case _ =>
            validatedAction.successResponse(request)

      case None =>
        validatedAction.successResponse(request)
  }

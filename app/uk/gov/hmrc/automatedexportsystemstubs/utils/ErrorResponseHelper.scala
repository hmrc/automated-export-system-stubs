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

package uk.gov.hmrc.automatedexportsystemstubs.utils

import play.api.mvc.{Result, Results}
import uk.gov.hmrc.automatedexportsystemstubs.models.{ErrorDetail, SourceFaultDetail}

object ErrorResponseHelper:

  def createErrorResponse(
    status:        Int,
    correlationId: String,
    errorMessage:  String,
    detail:        String
  ): Result =

    val errorDetail = ErrorDetail(
      timestamp = DateHelper.currentIsoTimestamp,
      correlationId = correlationId,
      errorCode = status.toString,
      errorMessage = errorMessage,
      source = "AES front end",
      sourceFaultDetail = SourceFaultDetail(
        detail = Seq(detail)
      )
    )

    Results
      .Status(status)(
        ErrorDetail.toXml(errorDetail)
      )
      .as("application/xml")

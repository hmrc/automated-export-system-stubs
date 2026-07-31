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

import play.api.http.Status
import play.api.mvc.BodyParsers
import play.api.test.Helpers.*
import play.api.test.Helpers
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.helpers.{AllMocks, BaseSpec}
import org.mockito.Mockito.when

class MessageControllerSpec extends BaseSpec with AllMocks:

  trait Setup:
    val requiredHeaders = Map("some-header" -> "header-val", "another-header" -> "another")
    when(mockAppConfig.requiredHeaders).thenReturn(requiredHeaders)
    val validatedRequestAction = ValidatedRequestAction(mock[BodyParsers.Default], mockAppConfig)
    val controller             = new MessageController(Helpers.stubControllerComponents(), validatedRequestAction)

  "POST /" - {

    "return 204 when all required headers are provided" in new Setup:
      val requestWithHeaders = fakeRequest.withHeaders(requiredHeaders.toSeq: _*)
      val result             = controller.message()(requestWithHeaders)
      status(result) shouldBe Status.NO_CONTENT

    "return 400 when required headers are missing" in new Setup:
      val requestWithHeaders = fakeRequest.withHeaders(("some-header", "header-value"))
      val result             = controller.message()(requestWithHeaders)
      status(result) shouldBe Status.BAD_REQUEST

  }

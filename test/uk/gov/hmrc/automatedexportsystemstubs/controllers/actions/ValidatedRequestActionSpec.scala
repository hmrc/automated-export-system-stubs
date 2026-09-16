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

package uk.gov.hmrc.automatedexportsystemstubs.controllers.actions

import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.*
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.*
import uk.gov.hmrc.automatedexportsystemstubs.config.AppConfig
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.request.ValidatedRequest
import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec

import scala.concurrent.Future

class ValidatedRequestActionSpec extends BaseSpec {

  trait Setup:
    when(mockAppConfig.notificationToken).thenReturn("test-token")
    val cc          = stubControllerComponents()
    val bodyParsers = new BodyParsers.Default(cc.parsers)

  "ValidatedRequestAction wildcard matching" - {

    "BAD_REQUEST when expected header value is '*'" in new Setup {
      when(mockAppConfig.requiredHeaders).thenReturn(
        Map("x-required" -> "*")
      )

      val action = ValidatedRequestAction(bodyParsers, mockAppConfig)

      val request = FakeRequest("GET", "/test")
        .withHeaders("x-required" -> "*")

      val result =
        action.invokeBlock(request, (_: ValidatedRequest[AnyContent]) => Future.successful(Results.Ok))

      status(result) shouldBe Status.BAD_REQUEST
    }
    "BAD_REQUEST when expected header value is empty" in new Setup {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.requiredHeaders).thenReturn(
        Map("x-required" -> "*")
      )

      val action = ValidatedRequestAction(bodyParsers, mockAppConfig)

      val request = FakeRequest("GET", "/test")
        .withHeaders("x-required" -> "")

      val result =
        action.invokeBlock(request, (_: ValidatedRequest[AnyContent]) => Future.successful(Results.Ok))

      status(result) shouldBe Status.BAD_REQUEST
    }

    "BAD_REQUEST when date header value is not a date" in new Setup {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.requiredHeaders).thenReturn(
        Map("date" -> "*")
      )

      val action = ValidatedRequestAction(bodyParsers, mockAppConfig)

      val request = FakeRequest("GET", "/test")
        .withHeaders("date" -> "some-date")

      val result =
        action.invokeBlock(request, (_: ValidatedRequest[AnyContent]) => Future.successful(Results.Ok))

      status(result) shouldBe Status.BAD_REQUEST
    }

    "BAD_REQUEST when wildcard header is missing" in new Setup {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.requiredHeaders).thenReturn(
        Map("x-required" -> "*")
      )

      val action = ValidatedRequestAction(bodyParsers, mockAppConfig)

      val request = FakeRequest("GET", "/test")

      val result =
        action.invokeBlock(request, (_: ValidatedRequest[AnyContent]) => Future.successful(Results.Ok))

      status(result)                shouldBe Status.BAD_REQUEST
      Helpers.contentAsString(result) should include("errorDetail")
      Helpers.contentAsString(result) should include("<errorCode>400</errorCode>")
    }

    "OK when all headers are present" in new Setup {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.requiredHeaders).thenReturn(
        Map(
          "x-wild"  -> "*",
          "x-exact" -> "expected",
          "date"    -> "Fri, 31 Jul 2026 10:30:00 GMT"
        )
      )

      val action = ValidatedRequestAction(bodyParsers, mockAppConfig)

      val request = FakeRequest("GET", "/test")
        .withHeaders(
          "x-wild"  -> "any-value",
          "x-exact" -> "wrong"
        )

      val result =
        action.invokeBlock(request, (_: ValidatedRequest[AnyContent]) => Future.successful(Results.Ok))

      status(result) shouldBe Status.BAD_REQUEST
    }
  }

}

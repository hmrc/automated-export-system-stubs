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
import play.api.test.Helpers.*
import play.api.test.Helpers
import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec

class MessageControllerSpec extends BaseSpec:
  private val controller = new MessageController(Helpers.stubControllerComponents())

  "GET /" - {
    "return 200" in:
      val result = controller.message()(fakeRequest)
      status(result) shouldBe Status.OK

  }

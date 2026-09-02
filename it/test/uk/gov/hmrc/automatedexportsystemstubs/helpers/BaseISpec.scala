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

package uk.gov.hmrc.automatedexportsystemstubs.helpers

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.{Status, *}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.*
import uk.gov.hmrc.automatedexportsystemstubs.helpers.{AllMocks, WireMockSupport}

trait BaseISpec
    extends AnyFreeSpecLike
    with AllMocks
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with Matchers
    with Inspectors
    with ScalaFutures
    with DefaultAwaitTimeout
    with Writeables
    with FutureAwaits
    with EssentialActionCaller
    with RouteInvokers
    with LoneElement
    with Inside
    with OptionValues
    with Results
    with HeaderNames
    with Status
    with MimeTypes
    with HttpProtocol
    with HttpVerbs
    with ResultExtractors
    with WireMockSupport
    with Eventually:

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  if (!mockServer.isRunning) {
    mockServer.start()
    com.github.tomakehurst.wiremock.client.WireMock.configureFor(mockServerHost, mockServer.port())
  }

  override lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.aes-notifications.url" -> s"http://$mockServerHost:$mockServerPort/automated-export-system-notifications/notification",
        "microservice.services.aes-notifications.auth-token" -> "auth-token",
        "microservice.services.auth.host"                    -> mockServerHost,
        "microservice.services.auth.port"                    -> mockServerPort,
        "metrics.enabled"                                    -> "false"
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubFor(
      post(urlEqualTo("/notification"))
        .willReturn(
          aResponse()
            .withStatus(204)
            .withBody("")
        )
    )
  }

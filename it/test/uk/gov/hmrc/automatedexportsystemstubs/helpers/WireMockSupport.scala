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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WireMockSupport extends BeforeAndAfterAll with BeforeAndAfterEach { me: Suite =>

  val mockServerHost: String = "localhost"
  val mockServer = new WireMockServer(wireMockConfig().dynamicPort())

  def mockServerPort: Int    = mockServer.port()
  def mockServerUrl:  String = s"http://$mockServerHost:$mockServerPort"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    if (!mockServer.isRunning) mockServer.start()
    com.github.tomakehurst.wiremock.client.WireMock.configureFor(mockServerHost, mockServer.port())
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    mockServer.resetAll()
  }

  override protected def afterAll(): Unit = {
    if (mockServer.isRunning) mockServer.stop()
    super.afterAll()
  }
}

package uk.gov.hmrc.automatedexportsystemstubs.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateHelper:
  def currentHttpDate: String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())


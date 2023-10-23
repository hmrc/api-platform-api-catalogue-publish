/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformapicataloguepublish.apidefinition.models

import scala.collection.immutable
import enumeratum._

sealed trait AuthType extends EnumEntry

object AuthType extends Enum[AuthType] with PlayJsonEnum[AuthType] {

  val values: immutable.IndexedSeq[AuthType] = findValues

  case object NONE        extends AuthType
  case object APPLICATION extends AuthType
  case object USER        extends AuthType

}
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

package uk.gov.hmrc.apicataloguepublish.apidefinition.models

sealed trait ApiAccess

object ApiAccess {

  case object PUBLIC                           extends ApiAccess
  case class Private(isTrial: Boolean = false) extends ApiAccess

  def apiAccessToDescription(accessType: ApiAccess): String = {
    accessType match {
      case ApiAccess.PUBLIC     => "This is a public API."
      case _: ApiAccess.Private => "This is a private API."
    }
  }
}

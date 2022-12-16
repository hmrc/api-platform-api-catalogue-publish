/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformapicataloguepublish.parser

trait OasStringUtils {
  
  val oasStringWithDescription = 
    """openapi: 3.0.0
info:
  title: Agent Authorisation
  description: A description.
  version: "1.0"
servers:
  -
    url: https://api.service.hmrc.gov.uk/
consumes:
  - application/json
  - application/hal+json
produces:
  - application/json
  - application/hal+json
schemes:
  - HTTPS
externalDocs:
  description: docs/overview.md
  x-amf-title: Overview
x-amf-userDocumentation:
  -
    content: docs/change-log.md
    title: Change Log
  -
    content: docs/why-use-this-api.md
    title: Why use this API?
  -
    content: docs/usage-scenario.md
    title: Usage scenario
  -
    content: |
      When an API changes in a way that is backwards-incompatible, we increase the version number of the API. 
      See our [reference guide](/api-documentation/docs/reference-guide#versioning) for more on
      versioning.
    title: Versioning
  -
    content: "We use standard HTTP status codes to show whether an API request succeeded or not. They are usually in the range:\n* 200 to 299 if it succeeded, including code 202 if it was accepted by an API that needs to wait for further action\n* 400 to 499 if it failed because of a client error by your application\n* 500 to 599 if it failed because of an error on our server\n\nErrors specific to each API are shown in the Endpoints section, under Response. \nSee our [reference guide](/api-documentation/docs/reference-guide#errors) for more on errors.\n\n"
    title: Errors
paths:
  /agents/{arn}:
    parameters:
      -
        name: arn
        required: true
        in: path
        schema:
          type: string
  /agents/{arn}/invitations:
    x-amf-is:
      - headers.acceptHeader
      - mustBeAnAgent
      - agentSubscriptionRequired
      - permissionOnAgencyRequired
      - headerErrors
      - standardErrors
    parameters:
      -
        name: arn
        description: docs/arn_see_here.md
        required: true
        in: path
        schema:
          description: docs/arn_see_here.md
          example: AARN9999999
          type: string
    post:
      operationId: Create a new authorisation
      x-amf-is:
        - serviceSpecified
        - invitationMustHaveSupportedClientType
        - clientIdSpecified
        - clientRegistrationRequired
        - knownFactMatches
        - pendingAuthorisationExists
        - activeAuthorisationExists
        - validatePayload
      requestBody:
        content:
          application/json:
            schema:
              description: Create a new authorisation request. The request will expire after 21 days.
              x-amf-examples:
                example-1: examples/post-agency-invitations-example.json
                example-2: examples/post-agency-invitations-vat-example.json
      responses:
        "204":
          description: The authorisation request was created successfully.
          headers:
            Location:
              description: Location of the authorisation request.
              required: true
              schema:
                description: Location of the authorisation request.
                example: /agents/AARN9999999/invitations/CS5AK7O8FPC43
                type: string
      security:
        -
          sec.oauth_2_0:
            - "null"
      x-annotations.scope: write:sent-invitations
    get:
      operationId: Get all authorisation requests for the last 30 days
      responses:
        "200":
          description: ""
          content:
            application/json:
              schema:
                description: Returns all authorisation requests for the last 30 days.
                x-amf-examples:
                  pending and responded: examples/get-agency-invitations-example.json
        "204":
          description: The agent has no authorisation requests for the last 30 days.
      security:
        -
          sec.oauth_2_0:
            - "null"
      x-annotations.scope: read:sent-invitations
  /agents/{arn}/invitations/{invitationId}:
    x-amf-is:
      - headers.acceptHeader
      - mustBeAnAgent
      - agentSubscriptionRequired
      - permissionOnAgencyRequired
      - invitationSpecified
      - headerErrors
      - standardErrors
    parameters:
      -
        name: arn
        description: docs/arn_see_here.md
        required: true
        in: path
        schema:
          description: docs/arn_see_here.md
          example: AARN9999999
          type: string
      -
        name: invitationId
        description: A unique authorisation request ID
        required: true
        in: path
        schema:
          description: A unique authorisation request ID
          example: CS5AK7O8FPC43
          type: string
    get:
      operationId: Get an invitation by id
      responses:
        "200":
          description: ""
          content:
            application/json:
              schema:
                description: Returns the authorisation request.
                x-amf-examples:
                  pending: examples/get-agency-invitation-pending-example.json
                  responded: examples/get-agency-invitation-responded-example.json
      security:
        -
          sec.oauth_2_0:
            - "null"
      x-annotations.scope: read:sent-invitations
    delete:
      operationId: Cancel an invitation by id
      x-amf-is:
        - invitationMustHaveValidStatus
      responses:
        "204":
          description: The authorisation request has been cancelled successfully.
      security:
        -
          sec.oauth_2_0:
            - "null"
      x-annotations.scope: write:cancel-invitations
  /agents/{arn}/relationships:
    x-amf-is:
      - headers.acceptHeader
      - clientRegistrationRequired
      - knownFactMatches
      - mustBeAnAgent
      - agentSubscriptionRequired
      - permissionOnAgencyRequired
      - relationshipNotFound
      - serviceSpecified
      - clientIdSpecified
      - headerErrors
      - standardErrors
    parameters:
      -
        name: arn
        description: docs/arn_see_here.md
        required: true
        in: path
        schema:
          description: docs/arn_see_here.md
          example: AARN9999999
          type: string
    post:
      operationId: Get Status of a Relationship
      requestBody:
        content:
          application/json:
            schema:
              description: Check Relationship based on the details received.
              x-amf-examples:
                example-1: examples/post-agency-check-relationship-itsa-example.json
                example-2: examples/post-agency-check-relationship-vat-example.json
      responses:
        "204":
          description: Relationship is active. Agent is authorised to act for the client.
      security:
        -
          sec.oauth_2_0:
            - "null"
      x-annotations.scope: read:check-relationship
components:
  x-amf-traits:
    permissionOnAgencyRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                noPermissionOnAgency:
                  description: The user that is signed in cannot access this authorisation request. Their details do not match the agent business that created the authorisation request.
                  value:
                    code: NO_PERMISSION_ON_AGENCY
                    message: The user that is signed in cannot access this authorisation request. Their details do not match the agent business that created the authorisation request.
    invitationMustHaveSupportedClientType:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidInvitationStatus:
                  description: The client type requested is not supported. Check the API documentation to find which client types are supported.
                  value:
                    code: CLIENT_TYPE_NOT_SUPPORTED
                    message: The client type requested is not supported. Check the API documentation to find which client types are supported.
    clientIdSpecified:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidClientId:
                  description: Client identifier must be in the correct format. Check the API documentation to find the correct format.
                  value:
                    code: CLIENT_ID_FORMAT_INVALID
                    message: Client identifier must be in the correct format. Check the API documentation to find the correct format.
                clientIdMatchesService:
                  description: The type of client Identifier provided cannot be used with the requested service. Check the API documentation for details of the correct client identifiers to use.
                  value:
                    code: CLIENT_ID_DOES_NOT_MATCH_SERVICE
                    message: The type of client Identifier provided cannot be used with the requested service. Check the API documentation for details of the correct client identifiers to use.
                invalidPostcode:
                  description: Postcode must be in the correct format. Check the API documentation to find the correct format.
                  value:
                    code: POSTCODE_FORMAT_INVALID
                    message: Postcode must be in the correct format. Check the API documentation to find the correct format.
                invalidVatRegistrationDate:
                  description: VAT registration date must be in the correct format. Check the API documentation to find the correct format.
                  value:
                    code: VAT_REG_DATE_FORMAT_INVALID
                    message: VAT registration date must be in the correct format. Check the API documentation to find the correct format.
    validatePayload:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The payload is invalid.
                  value:
                    code: INVALID_PAYLOAD
                    message: The payload is invalid.
    knownFactMatches:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                postcodeDoesNotMatch:
                  description: The postcode provided does not match HMRC's record for the client.
                  value:
                    code: POSTCODE_DOES_NOT_MATCH
                    message: The postcode provided does not match HMRC's record for the client.
                vatRegistrationDateDoesNotMatch:
                  description: The VAT registration date provided does not match HMRC's record for the client.
                  value:
                    code: VAT_REG_DATE_DOES_NOT_MATCH
                    message: The VAT registration date provided does not match HMRC's record for the client.
    headerErrors:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                badRequestUnsupportedVersion:
                  description: Missing or unsupported version found in 'Accept' header.
                  value:
                    code: BAD_REQUEST
                    message: Missing or unsupported version number
                badRequestUnsupportedContentType:
                  description: Missing or unsupported found in 'Accept' header.
                  value:
                    code: BAD_REQUEST
                    message: Missing or unsupported content-type.
        "406":
          body:
            application/json:
              type: types.errorResponse
              examples:
                acceptHeaderMissing:
                  description: Missing 'Accept' header.
                  value:
                    code: ACCEPT_HEADER_INVALID
                    message: Missing 'Accept' header.
                acceptHeaderInvalid:
                  description: Invalid 'Accept' header.
                  value:
                    code: ACCEPT_HEADER_INVALID
                    message: Invalid 'Accept' header
    pendingAuthorisationExists:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                duplicateAuthorisationRequest:
                  description: A pending invitation already exists between the agent and client for this service.
                  value:
                    code: DUPLICATE_AUTHORISATION_REQUEST
                    message: An authorisation request for this service has already been created and is awaiting the client’s response.
    invitationMustHaveValidStatus:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidInvitationStatus:
                  description: This authorisation request cannot be cancelled as the client has already responded to the request, or the request has expired.
                  value:
                    code: INVALID_INVITATION_STATUS
                    message: This authorisation request cannot be cancelled as the client has already responded to the request, or the request has expired.
    invitationSpecified:
      responses:
        "404":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The authorisation request cannot be found.
                  value:
                    code: INVITATION_NOT_FOUND
                    message: The authorisation request cannot be found.
    agentSubscriptionRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                agentNotSubscribed:
                  description: This agent needs to create an agent services account before they can use this service.
                  value:
                    code: AGENT_NOT_SUBSCRIBED
                    message: This agent needs to create an agent services account before they can use this service.
    standardErrors:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                badRequest:
                  description: Bad Request
                  value:
                    code: BAD_REQUEST
                    message: Bad Request
        "401":
          body:
            application/json:
              type: types.errorResponse
              examples:
                unauthorizedRequest:
                  description: Bearer token is missing or not authorized.
                  value:
                    code: UNAUTHORIZED
                    message: Bearer token is missing or not authorized.
        "500":
          body:
            application/json:
              type: types.errorResponse
              examples:
                internalServerRequest:
                  description: INTERNAL_SERVER_ERROR
                  value:
                    code: INTERNAL_SERVER_ERROR
                    message: Internal server error.
    activeAuthorisationExists:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                alreadyAuthorised:
                  description: An active relationship already exists between the agent and client for this service.
                  value:
                    code: ALREADY_AUTHORISED
                    message: The client has already authorised the agent for this service. The agent does not need ask the client for this authorisation again.
    serviceSpecified:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The service requested is not supported. Check the API documentation to find which services are supported.
                  value:
                    code: SERVICE_NOT_SUPPORTED
                    message: The service requested is not supported. Check the API documentation to find which services are supported.
    mustBeAnAgent:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                notAnAgent:
                  description: This user does not have a Government Gateway agent account. They need to create an Government Gateway agent account before they can use this service.
                  value:
                    code: NOT_AN_AGENT
                    message: This user does not have a Government Gateway agent account. They need to create an Government Gateway agent account before they can use this service.
    clientRegistrationRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                clientRegistrationNotFound:
                  description: The details provided for this client do not match HMRC's records.
                  value:
                    code: CLIENT_REGISTRATION_NOT_FOUND
                    message: The details provided for this client do not match HMRC's records.
    relationshipNotFound:
      responses:
        "404":
          body:
            application/json:
              type: types.errorResponse
              examples:
                notfoundrelationship:
                  description: Relationship is inactive. Agent is not authorised to act for this client.
                  value:
                    code: RELATIONSHIP_NOT_FOUND
                    message: Relationship is inactive. Agent is not authorised to act for this client.
x-amf-uses:
  sec: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/securitySchemes.raml
  headers: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/headers.raml
  annotations: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/annotations.raml
  types: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/types.raml
"""

val oasStringWithEnhancements = 
"""openapi: 3.0.0
info:
  title: Agent Authorisation
  description: "#  Overview\ndocs/overview.md\n#  Change Log\ndocs/change-log.md\n\
    #  Why use this API?\ndocs/why-use-this-api.md\n#  Usage scenario\ndocs/usage-scenario.md\n\
    #  Versioning\nWhen an API changes in a way that is backwards-incompatible, we\
    \ increase the version number of the API. \nSee our [reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide#versioning)\
    \ for more on\nversioning.\n\n#  Errors\nWe use standard HTTP status codes to\
    \ show whether an API request succeeded or not. They are usually in the range:\n\
    \n* 200 to 299 if it succeeded, including code 202 if it was accepted by an API\
    \ that needs to wait for further action\n\n* 400 to 499 if it failed because of\
    \ a client error by your application\n\n* 500 to 599 if it failed because of an\
    \ error on our server\n\nErrors specific to each API are shown in the Endpoints\
    \ section, under Response. \nSee our [reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide#errors)\
    \ for more on errors.\n\n"
  version: "1.0"
  x-integration-catalogue:
    reviewed-date: 2021-12-25T12:00:00Z
    short-description: A description.
    platform: API_PLATFORM
    publisher-reference: apiName
externalDocs:
  description: docs/overview.md
  x-amf-title: Overview
servers:
- url: https://api.service.hmrc.gov.uk/
paths:
  /agents/{arn}:
    parameters:
    - name: arn
      in: path
      required: true
      style: simple
      explode: false
      schema:
        type: string
  /agents/{arn}/invitations:
    get:
      operationId: Get all authorisation requests for the last 30 days
      parameters:
      - name: Accept
        in: header
        description: "Specifies the response format and the version of the API to\
          \ be used. For example: `application/vnd.hmrc.1.0+json`"
        required: true
        schema:
          type: string
      - name: Authorization
        in: header
        description: "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`"
        required: true
        schema:
          type: string
      responses:
        "200":
          description: ""
          content:
            application/json:
              schema:
                description: Returns all authorisation requests for the last 30 days.
                x-amf-examples:
                  pending and responded: examples/get-agency-invitations-example.json
        "204":
          description: The agent has no authorisation requests for the last 30 days.
      security:
      - sec.oauth_2_0:
        - "null"
      x-annotations.scope: read:sent-invitations
    post:
      operationId: Create a new authorisation
      parameters:
      - name: Accept
        in: header
        description: "Specifies the response format and the version of the API to\
          \ be used. For example: `application/vnd.hmrc.1.0+json`"
        required: true
        schema:
          type: string
      - name: Authorization
        in: header
        description: "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`"
        required: true
        schema:
          type: string
      requestBody:
        content:
          application/json:
            schema:
              description: Create a new authorisation request. The request will expire
                after 21 days.
              x-amf-examples:
                example-1: examples/post-agency-invitations-example.json
                example-2: examples/post-agency-invitations-vat-example.json
      responses:
        "204":
          description: The authorisation request was created successfully.
          headers:
            Location:
              description: Location of the authorisation request.
              required: true
              style: simple
              explode: false
              schema:
                type: string
                description: Location of the authorisation request.
                example: /agents/AARN9999999/invitations/CS5AK7O8FPC43
      security:
      - sec.oauth_2_0:
        - "null"
      x-amf-is:
      - serviceSpecified
      - invitationMustHaveSupportedClientType
      - clientIdSpecified
      - clientRegistrationRequired
      - knownFactMatches
      - pendingAuthorisationExists
      - activeAuthorisationExists
      - validatePayload
      x-annotations.scope: write:sent-invitations
    parameters:
    - name: arn
      in: path
      description: docs/arn_see_here.md
      required: true
      style: simple
      explode: false
      schema:
        type: string
        description: docs/arn_see_here.md
        example: AARN9999999
    x-amf-is:
    - headers.acceptHeader
    - mustBeAnAgent
    - agentSubscriptionRequired
    - permissionOnAgencyRequired
    - headerErrors
    - standardErrors
  /agents/{arn}/invitations/{invitationId}:
    get:
      operationId: Get an invitation by id
      parameters:
      - name: Accept
        in: header
        description: "Specifies the response format and the version of the API to\
          \ be used. For example: `application/vnd.hmrc.1.0+json`"
        required: true
        schema:
          type: string
      - name: Authorization
        in: header
        description: "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`"
        required: true
        schema:
          type: string
      responses:
        "200":
          description: ""
          content:
            application/json:
              schema:
                description: Returns the authorisation request.
                x-amf-examples:
                  pending: examples/get-agency-invitation-pending-example.json
                  responded: examples/get-agency-invitation-responded-example.json
      security:
      - sec.oauth_2_0:
        - "null"
      x-annotations.scope: read:sent-invitations
    delete:
      operationId: Cancel an invitation by id
      parameters:
      - name: Accept
        in: header
        description: "Specifies the response format and the version of the API to\
          \ be used. For example: `application/vnd.hmrc.1.0+json`"
        required: true
        schema:
          type: string
      - name: Authorization
        in: header
        description: "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`"
        required: true
        schema:
          type: string
      responses:
        "204":
          description: The authorisation request has been cancelled successfully.
      security:
      - sec.oauth_2_0:
        - "null"
      x-amf-is:
      - invitationMustHaveValidStatus
      x-annotations.scope: write:cancel-invitations
    parameters:
    - name: arn
      in: path
      description: docs/arn_see_here.md
      required: true
      style: simple
      explode: false
      schema:
        type: string
        description: docs/arn_see_here.md
        example: AARN9999999
    - name: invitationId
      in: path
      description: A unique authorisation request ID
      required: true
      style: simple
      explode: false
      schema:
        type: string
        description: A unique authorisation request ID
        example: CS5AK7O8FPC43
    x-amf-is:
    - headers.acceptHeader
    - mustBeAnAgent
    - agentSubscriptionRequired
    - permissionOnAgencyRequired
    - invitationSpecified
    - headerErrors
    - standardErrors
  /agents/{arn}/relationships:
    post:
      operationId: Get Status of a Relationship
      parameters:
      - name: Accept
        in: header
        description: "Specifies the response format and the version of the API to\
          \ be used. For example: `application/vnd.hmrc.1.0+json`"
        required: true
        schema:
          type: string
      - name: Authorization
        in: header
        description: "An OAuth 2.0 Bearer Token. For example: Bearer `bb7fed3fe10dd235a2ccda3d50fb`"
        required: true
        schema:
          type: string
      requestBody:
        content:
          application/json:
            schema:
              description: Check Relationship based on the details received.
              x-amf-examples:
                example-1: examples/post-agency-check-relationship-itsa-example.json
                example-2: examples/post-agency-check-relationship-vat-example.json
      responses:
        "204":
          description: Relationship is active. Agent is authorised to act for the
            client.
      security:
      - sec.oauth_2_0:
        - "null"
      x-annotations.scope: read:check-relationship
    parameters:
    - name: arn
      in: path
      description: docs/arn_see_here.md
      required: true
      style: simple
      explode: false
      schema:
        type: string
        description: docs/arn_see_here.md
        example: AARN9999999
    x-amf-is:
    - headers.acceptHeader
    - clientRegistrationRequired
    - knownFactMatches
    - mustBeAnAgent
    - agentSubscriptionRequired
    - permissionOnAgencyRequired
    - relationshipNotFound
    - serviceSpecified
    - clientIdSpecified
    - headerErrors
    - standardErrors
components:
  x-amf-traits:
    permissionOnAgencyRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                noPermissionOnAgency:
                  description: The user that is signed in cannot access this authorisation
                    request. Their details do not match the agent business that created
                    the authorisation request.
                  value:
                    code: NO_PERMISSION_ON_AGENCY
                    message: The user that is signed in cannot access this authorisation
                      request. Their details do not match the agent business that
                      created the authorisation request.
    invitationMustHaveSupportedClientType:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidInvitationStatus:
                  description: The client type requested is not supported. Check the
                    API documentation to find which client types are supported.
                  value:
                    code: CLIENT_TYPE_NOT_SUPPORTED
                    message: The client type requested is not supported. Check the
                      API documentation to find which client types are supported.
    clientIdSpecified:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidClientId:
                  description: Client identifier must be in the correct format. Check
                    the API documentation to find the correct format.
                  value:
                    code: CLIENT_ID_FORMAT_INVALID
                    message: Client identifier must be in the correct format. Check
                      the API documentation to find the correct format.
                clientIdMatchesService:
                  description: The type of client Identifier provided cannot be used
                    with the requested service. Check the API documentation for details
                    of the correct client identifiers to use.
                  value:
                    code: CLIENT_ID_DOES_NOT_MATCH_SERVICE
                    message: The type of client Identifier provided cannot be used
                      with the requested service. Check the API documentation for
                      details of the correct client identifiers to use.
                invalidPostcode:
                  description: Postcode must be in the correct format. Check the API
                    documentation to find the correct format.
                  value:
                    code: POSTCODE_FORMAT_INVALID
                    message: Postcode must be in the correct format. Check the API
                      documentation to find the correct format.
                invalidVatRegistrationDate:
                  description: VAT registration date must be in the correct format.
                    Check the API documentation to find the correct format.
                  value:
                    code: VAT_REG_DATE_FORMAT_INVALID
                    message: VAT registration date must be in the correct format.
                      Check the API documentation to find the correct format.
    validatePayload:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The payload is invalid.
                  value:
                    code: INVALID_PAYLOAD
                    message: The payload is invalid.
    knownFactMatches:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                postcodeDoesNotMatch:
                  description: The postcode provided does not match HMRC's record
                    for the client.
                  value:
                    code: POSTCODE_DOES_NOT_MATCH
                    message: The postcode provided does not match HMRC's record for
                      the client.
                vatRegistrationDateDoesNotMatch:
                  description: The VAT registration date provided does not match HMRC's
                    record for the client.
                  value:
                    code: VAT_REG_DATE_DOES_NOT_MATCH
                    message: The VAT registration date provided does not match HMRC's
                      record for the client.
    headerErrors:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                badRequestUnsupportedVersion:
                  description: Missing or unsupported version found in 'Accept' header.
                  value:
                    code: BAD_REQUEST
                    message: Missing or unsupported version number
                badRequestUnsupportedContentType:
                  description: Missing or unsupported found in 'Accept' header.
                  value:
                    code: BAD_REQUEST
                    message: Missing or unsupported content-type.
        "406":
          body:
            application/json:
              type: types.errorResponse
              examples:
                acceptHeaderMissing:
                  description: Missing 'Accept' header.
                  value:
                    code: ACCEPT_HEADER_INVALID
                    message: Missing 'Accept' header.
                acceptHeaderInvalid:
                  description: Invalid 'Accept' header.
                  value:
                    code: ACCEPT_HEADER_INVALID
                    message: Invalid 'Accept' header
    pendingAuthorisationExists:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                duplicateAuthorisationRequest:
                  description: A pending invitation already exists between the agent
                    and client for this service.
                  value:
                    code: DUPLICATE_AUTHORISATION_REQUEST
                    message: An authorisation request for this service has already
                      been created and is awaiting the client’s response.
    invitationMustHaveValidStatus:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invalidInvitationStatus:
                  description: "This authorisation request cannot be cancelled as\
                    \ the client has already responded to the request, or the request\
                    \ has expired."
                  value:
                    code: INVALID_INVITATION_STATUS
                    message: "This authorisation request cannot be cancelled as the\
                      \ client has already responded to the request, or the request\
                      \ has expired."
    invitationSpecified:
      responses:
        "404":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The authorisation request cannot be found.
                  value:
                    code: INVITATION_NOT_FOUND
                    message: The authorisation request cannot be found.
    agentSubscriptionRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                agentNotSubscribed:
                  description: This agent needs to create an agent services account
                    before they can use this service.
                  value:
                    code: AGENT_NOT_SUBSCRIBED
                    message: This agent needs to create an agent services account
                      before they can use this service.
    standardErrors:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                badRequest:
                  description: Bad Request
                  value:
                    code: BAD_REQUEST
                    message: Bad Request
        "401":
          body:
            application/json:
              type: types.errorResponse
              examples:
                unauthorizedRequest:
                  description: Bearer token is missing or not authorized.
                  value:
                    code: UNAUTHORIZED
                    message: Bearer token is missing or not authorized.
        "500":
          body:
            application/json:
              type: types.errorResponse
              examples:
                internalServerRequest:
                  description: INTERNAL_SERVER_ERROR
                  value:
                    code: INTERNAL_SERVER_ERROR
                    message: Internal server error.
    activeAuthorisationExists:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                alreadyAuthorised:
                  description: An active relationship already exists between the agent
                    and client for this service.
                  value:
                    code: ALREADY_AUTHORISED
                    message: The client has already authorised the agent for this
                      service. The agent does not need ask the client for this authorisation
                      again.
    serviceSpecified:
      responses:
        "400":
          body:
            application/json:
              type: types.errorResponse
              examples:
                invitationNotFound:
                  description: The service requested is not supported. Check the API
                    documentation to find which services are supported.
                  value:
                    code: SERVICE_NOT_SUPPORTED
                    message: The service requested is not supported. Check the API
                      documentation to find which services are supported.
    mustBeAnAgent:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                notAnAgent:
                  description: This user does not have a Government Gateway agent
                    account. They need to create an Government Gateway agent account
                    before they can use this service.
                  value:
                    code: NOT_AN_AGENT
                    message: This user does not have a Government Gateway agent account.
                      They need to create an Government Gateway agent account before
                      they can use this service.
    clientRegistrationRequired:
      responses:
        "403":
          body:
            application/json:
              type: types.errorResponse
              examples:
                clientRegistrationNotFound:
                  description: The details provided for this client do not match HMRC's
                    records.
                  value:
                    code: CLIENT_REGISTRATION_NOT_FOUND
                    message: The details provided for this client do not match HMRC's
                      records.
    relationshipNotFound:
      responses:
        "404":
          body:
            application/json:
              type: types.errorResponse
              examples:
                notfoundrelationship:
                  description: Relationship is inactive. Agent is not authorised to
                    act for this client.
                  value:
                    code: RELATIONSHIP_NOT_FOUND
                    message: Relationship is inactive. Agent is not authorised to
                      act for this client.
x-amf-userDocumentation:
- content: docs/change-log.md
  title: Change Log
- content: docs/why-use-this-api.md
  title: Why use this API?
- content: docs/usage-scenario.md
  title: Usage scenario
- content: "When an API changes in a way that is backwards-incompatible, we increase\
    \ the version number of the API. \nSee our [reference guide](/api-documentation/docs/reference-guide#versioning)\
    \ for more on\nversioning.\n"
  title: Versioning
- content: "We use standard HTTP status codes to show whether an API request succeeded\
    \ or not. They are usually in the range:\n* 200 to 299 if it succeeded, including\
    \ code 202 if it was accepted by an API that needs to wait for further action\n\
    * 400 to 499 if it failed because of a client error by your application\n* 500\
    \ to 599 if it failed because of an error on our server\n\nErrors specific to\
    \ each API are shown in the Endpoints section, under Response. \nSee our [reference\
    \ guide](/api-documentation/docs/reference-guide#errors) for more on errors.\n\
    \n"
  title: Errors
x-amf-uses:
  sec: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/securitySchemes.raml
  headers: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/headers.raml
  annotations: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/annotations.raml
  types: https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/types.raml
"""
}
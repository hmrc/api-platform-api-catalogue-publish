
# api-platform-api-catalogue-publish

The api-platform-api-catalogue-publish publishes APIs to the [API Catalogue](https://admin.tax.service.gov.uk/api-catalogue).

## POST /publish/:serviceName endpoint

[Deploy Service in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/deploy-service) runs the
[publish API script](https://github.com/hmrc/api-platform-scripts/blob/main/publish_api.py)
which calls POST /publish/:serviceName endpoint on this service to publish the API onto the [API Catalogue](https://admin.tax.service.gov.uk/api-catalogue).
It only publishes APIs which have an OAS specification.

## POST /publish-all endpoint
This endpoint was used to migrate all API Platform APIs into the API Catalogue.
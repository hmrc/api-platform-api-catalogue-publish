
# api-platform-api-catalogue-publish

The api-platform-api-catalogue-publish publishes APIs to the [API Catalogue](https://admin.tax.service.gov.uk/api-catalogue).

## POST /publish/:serviceName endpoint

The [deploy-microservice job in Jenkins](https://orchestrator.tools.production.tax.service.gov.uk/job/deploy-microservice/) runs the
[publish API script](https://github.com/hmrc/api-platform-scripts/blob/main/publish_api.py)
which calls POST /publish/:serviceName endpoint on this service to publish the API onto the [API Catalogue](https://admin.tax.service.gov.uk/api-catalogue).

This is also called by clicking on the Publish button on [this hidden page](https://admin.qa.tax.service.gov.uk/api-gatekeeper/apicatalogue/start) in api-gatekeeper-frontend. 

## POST /publish-all endpoint
This is called by clicking on the Publish All Apis button on [this hidden page](https://admin.qa.tax.service.gov.uk/api-gatekeeper/apicatalogue/start) in api-gatekeeper-frontend.
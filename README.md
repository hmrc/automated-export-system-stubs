
# automated-export-system-stubs

This is the stubs for the Automated Export System. 
It contains a single endpoint, and returns either a 204 response of a 400, 401, or 500 response

## Running the stubs

It runs on port 5002 and is included in the service manager profile AUTOMATED_EXPORT_SYSTEM_ALL

## Technical documentation

The end point is POST /cds/aesIE507Request/v1
The stubs are responsible for 
- receiving and validating requests from the back end service
- simulating async requests and
- submitting these request to the notification service

Briefly it
- checks for the absence or presence of the required headers
- checks for the absence or presence of payload elements that indicate a specific sync response and respond accordingly
- checks for the absence or presence of payload elements that indicate a specific async error and creates appropriate async payload
- makes a call to the notification service with the required payload

### Further documentation

[IE507 Request to stubs](https://confluence.tools.tax.service.gov.uk/spaces/AES/pages/1321467912/IE507+Request+to+stubs).
[Request to notification service](https://confluence.tools.tax.service.gov.uk/spaces/AES/pages/1367278073/IE906+Async+error+mapping)


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


# automated-export-system-stubs

This is the stubs for the Automated Export System. 
It contains a single endpoint, and returns either a 204 response, 400, 401, or 500 response

## Running the stubs

It runs on port 5002 and is included in the service manager profile AUTOMATED_EXPORT_SERVICE_ALL

## Technical documentation
The stubs are responsible for 
- receiving and validating requests from the back end service and
- simulating async requests to the notification service

Briefly it
- checks for the absence or presence of the required headers
- checks for the absence or presence of payload elements that indicate a specific sync response and respond accordingly
- checks for the absence or presence of payload elements that indicate a specific async error and creates appropriate async payload
- makes a call to the notification service with the required payload

## Endpoint
`POST /cds/aesIE507Request/v1`
```<?xml version="1.0" encoding="UTF-8" ?>
  <n1:CC507C xmlns:n1="http://ecs.dgtaxud.ec">
  <Header>
    <messageSender>3GB123456789012-ABC12345678</messageSender>
    <messageRecipient>NECA.XI</messageRecipient>
    <preparationDateAndTime>2026-06-17T10:20:30</preparationDateAndTime>
    <messageIdentification>MSGID-20260617-001</messageIdentification>
    <messageType>CC507C</messageType>
  </Header>

  <Body>
    <ExportOperation>
      <type>1</type>
      <MRN>26GB0000X6524786352</MRN>
      <discrepanciesExist>0</discrepanciesExist>
      <splitIndicator>0</splitIndicator>
    </ExportOperation>

    <CustomsOfficeOfExitActual>
      <referenceNumber>IEARK100</referenceNumber>
    </CustomsOfficeOfExitActual>

    <GoodsShipment>
      <Consignment>
        <modeOfTransportAtTheBorder>1</modeOfTransportAtTheBorder>
        <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
        <parentUCRID>GB/ABC-12345</parentUCRID>

        <TransportEquipment>
          <sequenceNumber>1</sequenceNumber>
          <containerIdentificationNumber>CONT1234567890123</containerIdentificationNumber>
          <numberOfSeals>2</numberOfSeals>

          <Seal>
            <sequenceNumber>1</sequenceNumber>
            <identifier>SEAL123</identifier>
          </Seal>
          <Seal>
            <sequenceNumber>2</sequenceNumber>
            <identifier>SEAL124</identifier>
          </Seal> 

          <GoodsReference>
            <sequenceNumber>1</sequenceNumber>
            <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
          </GoodsReference>
           <GoodsReference>
            <sequenceNumber>2</sequenceNumber>
            <declarationGoodsItemNumber>10</declarationGoodsItemNumber>
          <GoodsReference>
            <sequenceNumber>3</sequenceNumber>
            <declarationGoodsItemNumber>11</declarationGoodsItemNumber>
          </GoodsReference>
           <GoodsReference>
            <sequenceNumber>4</sequenceNumber>
            <declarationGoodsItemNumber>12</declarationGoodsItemNumber>
          </GoodsReference> 
        </TransportEquipment>

        <LocationOfGoods>
          <typeOfLocation>A</typeOfLocation>
          <qualifierOfIdentification>B</qualifierOfIdentification>
          <authorisationNumber>AUTH12345</authorisationNumber>
          <additionalIdentifier>AD01</additionalIdentifier>
          <UNLocode>UNLOCODE123</UNLocode>
        </LocationOfGoods>

        <ActiveBorderTransportMeans>
          <typeOfIdentification>20</typeOfIdentification>
          <identificationNumber>IDNUMBER123</identificationNumber>
        </ActiveBorderTransportMeans>

        <TransportDocument>
          <sequenceNumber>1</sequenceNumber>
          <type>2</type>
          <referenceNumber>REF123</referenceNumber>
        </TransportDocument>
         <TransportDocument>
          <sequenceNumber>2</sequenceNumber>
          <type>2</type>
          <referenceNumber>REF124</referenceNumber>
        </TransportDocument> 
      </Consignment>

      <GoodsItem>
        <declarationGoodsItemNumber>2</declarationGoodsItemNumber>

        <Commodity>
          <GoodsMeasure>
            <grossMass>1000.500000</grossMass>
            <netMass>900.500000</netMass>
          </GoodsMeasure>
        </Commodity>

        <Packaging>
          <sequenceNumber>1</sequenceNumber>
          <typeOfPackages>P</typeOfPackages>
          <numberOfPackages>10</numberOfPackages>
          <shippingMarks>MARKS123</shippingMarks>
        </Packaging>
        <Packaging>
          <sequenceNumber>2</sequenceNumber>
          <typeOfPackages>P</typeOfPackages>
          <numberOfPackages>10</numberOfPackages>
          <shippingMarks>MARKS1234</shippingMarks>
        </Packaging> 
      </GoodsItem>
       <GoodsItem>
        <declarationGoodsItemNumber>1</declarationGoodsItemNumber>

        <Commodity>
          <GoodsMeasure>
            <grossMass>1000.500000</grossMass>
            <netMass>900.500000</netMass>
          </GoodsMeasure>
        </Commodity>

        <Packaging>
          <sequenceNumber>1</sequenceNumber>
          <typeOfPackages>PA</typeOfPackages>
          <numberOfPackages>10</numberOfPackages>
          <shippingMarks>MARKS123</shippingMarks>
        </Packaging>
        <Packaging>
          <sequenceNumber>2</sequenceNumber>
          <typeOfPackages>PA</typeOfPackages>
          <numberOfPackages>10</numberOfPackages>
          <shippingMarks>MARKS1234</shippingMarks>
        </Packaging> 
      </GoodsItem> 
    </GoodsShipment>
  </Body>
</n1:CC507C>
```

### Further documentation

[IE507 Request to stubs](https://confluence.tools.tax.service.gov.uk/spaces/AES/pages/1321467912/IE507+Request+to+stubs).
[Request to notification service](https://confluence.tools.tax.service.gov.uk/spaces/AES/pages/1367278073/IE906+Async+error+mapping)


### License

This code is open source software licensed under the [Apache 2.0 License](LICENSE).

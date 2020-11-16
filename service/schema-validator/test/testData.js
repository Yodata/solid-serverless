module.exports = {
  "successData": {
      "event" : {
      "topic": "realestate/franchise#listingreport",
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        "type": "CreateAction",
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "wrongTopic": {
      "event" : {
      "topic": "realestate/franchise#",
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        "type": "CreateAction",
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "withoutTopic": {
      "event" : {
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        "type": "CreateAction",
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "withoutSource": {
      "event" : {
      "topic": "realestate/franchise#listingreport",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        "type": "CreateAction",
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "withoutData": {
      "event" : {
      "topic": "realestate/franchise#listingreport",
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z"
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "wrongDataDotType": {
      "event" : {
      "topic": "realestate/franchise#listingreport",
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        "type": "UpdateAction",
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    },
  "withoutDataDotType": {
      "event" : {
      "topic": "realestate/franchise#listingreport",
      "source": "http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me",
      "agent": "http://{event-publisher}.example.com/profile/card#me",
      "instrument": "http://{event-producer}.example.com/profile/card#me",
      "time": "2020-08-14T03:19:08Z",
      "data": {
        
        "object": {
          "type": "RealEstateTransaction",
          "identifier": {
            "guruTransactionId": "0000074792"
          },
          "additionalProperty": {
            "batchid": "4136",
            "bhhsTransactionSequence": 1,
            "bhhsUnimprovedLandFlag": true
          },
          "transactionStatus": "ClosedTransactionStatus",
          "transactionType": "ST",
          "reportingOffice": "https://{officeid}.example.com/profile/card#me",
          "listingOffice": "https://{officeid}.example.com/profile/card#me",
          "buyerOffice": "https://{officeid}.example.com/profile/card#me",
          "commissionDate": "2020-08-14T03:19:08Z",
          "closeDate": "2020-08-14T03:19:08Z",
          "purchaseContractDate": "2020-08-14T03:19:08Z",
          "closePrice": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCI": {
            "type": "MonetaryAmount",
            "value": 123456.78,
            "currency": "USD"
          },
          "totalSalesProductionGCIDeduction": {
            "type": "MonetaryAmount",
            "value": 1234.56,
            "currency": "USD"
          },
          "object": {
            "type": "RealEstateProperty",
            "propertyType": "RESI",
            "propertySubType": "SingleFamilyPropertyType",
            "streetAddress": "1433 W 150 N",
            "addressLocality": "Springville",
            "addressRegion": "UT",
            "postalCode": "84663",
            "addressCountry": "USA",
            "universalPropertyId": "US-04015-N-R-11022331-N"
          },
          "referral": {
            "type": "Referral",
            "referredBy": {
              "type": "RealEstateOrganization",
              "id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"
            },
            "additionalProperty": {
              "isReferralYN": "Y",
              "bhssInNetworkReferralYN": "Y"
            }
          },
          "participant": [
            {
              "type": "TransactionParticipant",
              "roleName": "Buyer",
              "position": 1,
              "givenName": "Josh",
              "familyName": "Cold",
              "additionalName": "Stone",
              "email": "joe@example.com",
              "telephone": "8885550001",
              "affiliation": [
                "https://474912.bhhs.hsfaffiliates.com/profile/card#me"
              ]
            }
          ],
          "transactionEntry": [
            {
              "type": "TransactionEntry",
              "salesProductionUnit": 0.5,
              "salesProductionGCI": {
                "type": "MonetaryAmount",
                "value": 1234.56,
                "currency": "USD"
              },
              "recipient": {
                "type": "RealEstateAgent",
                "roleName": "ListingAgent",
                "id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me",
                "identifier": {
                  "guruAgentId": "218034"
                }
              }
            }
          ],
          "document": [
            {
              "type": "DigitalDocument",
              "name": "Sales Contract",
              "encodingFormat": "application/zip",
              "about": {
                "type": "Transaction",
                "identifier": {
                  "guruTransactionId": "0000074792"
                }
              },
              "url": "https://example.com/path/to/document.pdf"
            }
          ]
        },
        "instrument": {
          "type": "SoftwareApplication",
          "name": "Breaking BMS"
        }
      }
    },
        "config": {
            "schemaURL": "https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.listingreport.yaml"
        }
    }
}
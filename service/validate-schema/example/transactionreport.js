module.exports = {
	'topic': 'realestate/franchise#transactionreport',
	'source': 'http://{event-subject}.gotham-city-real-estate.example.com/profile/card#me',
	'agent': 'http://{event-publisher}.example.com/profile/card#me',
	'instrument': 'http://{event-producer}.example.com/profile/card#me',
	'time': '2020-10-15T22:57:06Z',
	'data': {
		'type': 'UpdateAction',
		'object': {
			'type': 'RealEstateTransaction',
			'identifier': {
				'bmsTransactionId': '0000074792'
			},
			'additionalProperty': {
				'batchid': 4136,
				'transactionSequence': 1,
				'unimprovedLandFlag': true
			},
			'transactionStatus': 'ClosedTransactionStatus',
			'transactionType': 'ST',
			'reportingOffice': 'https://{officeid}.example.com/profile/card#me',
			'listingOffice': 'https://{officeid}.example.com/profile/card#me',
			'buyerOffice': 'https://{officeid}.example.com/profile/card#me',
			'commissionDate': '2020-10-15T22:57:06Z',
			'closeDate': '2020-10-15T22:57:06Z',
			'purchaseContractDate': '2020-10-15T22:57:06Z',
			'closePrice': {
				'type': 'MonetaryAmount',
				'value': 123456.78,
				'currency': 'USD'
			},
			'totalSalesProductionGCI': {
				'type': 'MonetaryAmount',
				'value': 123456.78,
				'currency': 'USD'
			},
			'totalSalesProductionGCIDeduction': {
				'type': 'MonetaryAmount',
				'value': 1234.56,
				'currency': 'USD'
			},
			'object': {
				'type': 'RealEstateProperty',
				'apn': 'ABC-12345-XX-XXXX',
				'listingId': 'string',
				'streetAddress': '1007 Mountain Gate Rd',
				'addressRegion': 'NJ',
				'addressLocality': 'Gotham City',
				'postalCode': '10010',
				'addressCountry': 'US',
				'propertyType': 'RESI',
				'propertySubType': 'ApartmentPropertyType',
				'universalPropertyId': 'US-04015-N-R-11022331-N'
			},
			'referral': {
				'type': 'Referral',
				'additionalProperty': {
					'isReferralYN': 'Y',
					'inNetworkReferralYN': 'Y'
				},
				'referredBy': {
					'type': 'RealEstateOrganization',
					'id': 'http://example.com'
				}
			},
			'participant': [
				{
					'type': 'TransactionParticipant',
					'roleName': 'Buyer',
					'position': 1,
					'givenName': 'Bruce',
					'familyName': 'Wayne',
					'additionalName': 'Big',
					'email': 'user@example.com',
					'telephone': '+15558675309',
					'affiliation': [
						'https://{agentid}.example.com/profile/card#me'
					]
				}
			],
			'transactionEntry': [
				{
					'type': 'TransactionEntry',
					'salesProductionUnit': 0.5,
					'salesProductionGCI': {
						'type': 'MonetaryAmount',
						'value': 1234.56,
						'currency': 'USD'
					},
					'recipient': {
						'type': 'RealEstateAgent',
						'roleName': 'ListingAgent',
						'id': 'https://{entityid}.example.com/profile/card#me',
						'identifier': {
							'bmsAgentId': '1657897'
						}
					}
				}
			],
			'document': [
				{
					'type': 'DigitalDocument',
					'name': 'Sales Contract',
					'encodingFormat': 'application/zip',
					'about': {
						'type': 'Transaction',
						'identifier': {
							'guruTransactionId': '0000074792'
						}
					},
					'url': 'https://example.com/path/to/document.pdf'
				}
			]
		},
		'instrument': {
			'type': 'SoftwareApplication',
			'name': 'MyBMS'
		}
	}
}
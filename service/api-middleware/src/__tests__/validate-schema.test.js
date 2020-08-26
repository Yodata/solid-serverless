/* eslint-disable no-undef */

const validateSchema = require('../validate-schema')

describe('api-middleware.validate-schema', () => {

	test('allowed example/response', async () => {
		const event = {"request": {"method": "POST","headers": {"content-length": "3520","x-amzn-trace-id": "Root=1-5f449b9f-698c52ca224dee579eabd199","x-forwarded-proto": "https","x-api-key": "SlRe5LFQ3yw8vAwIADti1uebfYRmKrtnJf9tNtRBq9","host": "dave.bhhs.hsfaffiliates.com","x-forwarded-port": "443","content-type": "application/json","x-forwarded-for": "73.70.186.42","accept-encoding": "gzip, deflate, br","user-agent": "PostmanRuntime/7.26.3","accept": "*/*"},"url": "https://dave.bhhs.hsfaffiliates.com/publish/","body": "ewogICJ0b3BpYyI6ICJyZWFsZXN0YXRlL2ZyYW5jaGlzZSN0cmFuc2FjdGlvbnJlcG9ydCIsCiAgInNvdXJjZSI6ICJodHRwczovL2RhdmUuYmhocy5kZXYueW9kYXRhLmlvL3Byb2ZpbGUvY2FyZCNtZSIsCiAgInJlY2lwaWVudCI6ICJodHRwczovL21pMzAxLmJoaHMuZGV2LnlvZGF0YS5pby9wcm9maWxlL2NhcmQjbWUiLAogICJkYXRhIjogewogICAgInR5cGUiOiAiVXBkYXRlQWN0aW9uIiwKICAgICJvYmplY3QiOiB7CiAgICAgICJ0eXBlIjogIlJlYWxFc3RhdGVUcmFuc2FjdGlvbiIsCiAgICAgICJpZGVudGlmaWVyIjogewogICAgICAgICJndXJ1VHJhbnNhY3Rpb25JZCI6ICIwMDAwMDc0NzkyIgogICAgICB9LAogICAgICAiYWRkaXRpb25hbFByb3BlcnR5IjogewogICAgICAgICJiYXRjaGlkIjogIjQxMzYiLAogICAgICAgICJiaGhzVHJhbnNhY3Rpb25TZXF1ZW5jZSI6IDEsCiAgICAgICAgImJoaHNVbmltcHJvdmVkTGFuZEZsYWciOiB0cnVlCiAgICAgIH0sCiAgICAgICJ0cmFuc2FjdGlvblN0YXR1cyI6ICJDbG9zZWRUcmFuc2FjdGlvblN0YXR1cyIsCiAgICAgICJ0cmFuc2FjdGlvblR5cGUiOiAiU1QiLAogICAgICAicmVwb3J0aW5nT2ZmaWNlIjogImh0dHBzOi8ve29mZmljZWlkfS5leGFtcGxlLmNvbS9wcm9maWxlL2NhcmQjbWUiLAogICAgICAibGlzdGluZ09mZmljZSI6ICJodHRwczovL3tvZmZpY2VpZH0uZXhhbXBsZS5jb20vcHJvZmlsZS9jYXJkI21lIiwKICAgICAgImJ1eWVyT2ZmaWNlIjogImh0dHBzOi8ve29mZmljZWlkfS5leGFtcGxlLmNvbS9wcm9maWxlL2NhcmQjbWUiLAogICAgICAiY29tbWlzc2lvbkRhdGUiOiAiMjAyMC0wOC0xNFQwMzoxOTowOFoiLAogICAgICAiY2xvc2VEYXRlIjogIjIwMjAtMDgtMTRUMDM6MTk6MDhaIiwKICAgICAgInB1cmNoYXNlQ29udHJhY3REYXRlIjogIjIwMjAtMDgtMTRUMDM6MTk6MDhaIiwKICAgICAgImNsb3NlUHJpY2UiOiB7CiAgICAgICAgInR5cGUiOiAiTW9uZXRhcnlBbW91bnQiLAogICAgICAgICJ2YWx1ZSI6IDEyMzQ1Ni43OCwKICAgICAgICAiY3VycmVuY3kiOiAiVVNEIgogICAgICB9LAogICAgICAidG90YWxTYWxlc1Byb2R1Y3Rpb25HQ0kiOiB7CiAgICAgICAgInR5cGUiOiAiTW9uZXRhcnlBbW91bnQiLAogICAgICAgICJ2YWx1ZSI6IDEyMzQ1Ni43OCwKICAgICAgICAiY3VycmVuY3kiOiAiVVNEIgogICAgICB9LAogICAgICAidG90YWxTYWxlc1Byb2R1Y3Rpb25HQ0lEZWR1Y3Rpb24iOiB7CiAgICAgICAgInR5cGUiOiAiTW9uZXRhcnlBbW91bnQiLAogICAgICAgICJ2YWx1ZSI6IDEyMzQuNTYsCiAgICAgICAgImN1cnJlbmN5IjogIlVTRCIKICAgICAgfSwKICAgICAgIm9iamVjdCI6IHsKICAgICAgICAidHlwZSI6ICJSZWFsRXN0YXRlUHJvcGVydHkiLAogICAgICAgICJwcm9wZXJ0eVR5cGUiOiAiUkVTSSIsCiAgICAgICAgInByb3BlcnR5U3ViVHlwZSI6ICJTaW5nbGVGYW1pbHlQcm9wZXJ0eVR5cGUiLAogICAgICAgICJzdHJlZXRBZGRyZXNzIjogIjE0MzMgVyAxNTAgTiIsCiAgICAgICAgImFkZHJlc3NMb2NhbGl0eSI6ICJTcHJpbmd2aWxsZSIsCiAgICAgICAgImFkZHJlc3NSZWdpb24iOiAiVVQiLAogICAgICAgICJwb3N0YWxDb2RlIjogIjg0NjYzIiwKICAgICAgICAiYWRkcmVzc0NvdW50cnkiOiAiVVNBIiwKICAgICAgICAidW5pdmVyc2FsUHJvcGVydHlJZCI6ICJVUy0wNDAxNS1OLVItMTEwMjIzMzEtTiIKICAgICAgfSwKICAgICAgInJlZmVycmFsIjogewogICAgICAgICJ0eXBlIjogIlJlZmVycmFsIiwKICAgICAgICAicmVmZXJyZWRCeSI6IHsKICAgICAgICAgICJ0eXBlIjogIlJlYWxFc3RhdGVPcmdhbml6YXRpb24iLAogICAgICAgICAgImlkIjogImh0dHBzOi8vRkwzMDUuYmhocy5oc2ZhZmZpbGlhdGVzLmNvbS9wcm9maWxlL2NhcmQjbWUiCiAgICAgICAgfSwKICAgICAgICAiYWRkaXRpb25hbFByb3BlcnR5IjogewogICAgICAgICAgImlzUmVmZXJyYWxZTiI6ICJZIiwKICAgICAgICAgICJiaHNzSW5OZXR3b3JrUmVmZXJyYWxZTiI6ICJZIgogICAgICAgIH0KICAgICAgfSwKICAgICAgInBhcnRpY2lwYW50IjogWwogICAgICAgIHsKICAgICAgICAgICJ0eXBlIjogIlRyYW5zYWN0aW9uUGFydGljaXBhbnQiLAogICAgICAgICAgInJvbGVOYW1lIjogIkJ1eWVyIiwKICAgICAgICAgICJwb3NpdGlvbiI6IDEsCiAgICAgICAgICAiZ2l2ZW5OYW1lIjogIkpvc2giLAogICAgICAgICAgImZhbWlseU5hbWUiOiAiQ29sZCIsCiAgICAgICAgICAiYWRkaXRpb25hbE5hbWUiOiAiU3RvbmUiLAogICAgICAgICAgImVtYWlsIjogImpvZUBleGFtcGxlLmNvbSIsCiAgICAgICAgICAidGVsZXBob25lIjogIjg4ODU1NTAwMDEiLAogICAgICAgICAgImFmZmlsaWF0aW9uIjogWwogICAgICAgICAgICAiaHR0cHM6Ly80NzQ5MTIuYmhocy5oc2ZhZmZpbGlhdGVzLmNvbS9wcm9maWxlL2NhcmQjbWUiCiAgICAgICAgICBdCiAgICAgICAgfQogICAgICBdLAogICAgICAidHJhbnNhY3Rpb25FbnRyeSI6IFsKICAgICAgICB7CiAgICAgICAgICAidHlwZSI6ICJUcmFuc2FjdGlvbkVudHJ5IiwKICAgICAgICAgICJzYWxlc1Byb2R1Y3Rpb25Vbml0IjogMC41LAogICAgICAgICAgInNhbGVzUHJvZHVjdGlvbkdDSSI6IHsKICAgICAgICAgICAgInR5cGUiOiAiTW9uZXRhcnlBbW91bnQiLAogICAgICAgICAgICAidmFsdWUiOiAxMjM0LjU2LAogICAgICAgICAgICAiY3VycmVuY3kiOiAiVVNEIgogICAgICAgICAgfSwKICAgICAgICAgICJyZWNpcGllbnQiOiB7CiAgICAgICAgICAgICJ0eXBlIjogIlJlYWxFc3RhdGVBZ2VudCIsCiAgICAgICAgICAgICJyb2xlTmFtZSI6ICJMaXN0aW5nQWdlbnQiLAogICAgICAgICAgICAiaWQiOiAiaHR0cHM6Ly97YWdlbnRpZH0uYmhocy5oc2ZhZmZpbGlhdGVzLmNvbS9wcm9maWxlL2NhcmQjbWUiLAogICAgICAgICAgICAiaWRlbnRpZmllciI6IHsKICAgICAgICAgICAgICAiZ3VydUFnZW50SWQiOiAiMjE4MDM0IgogICAgICAgICAgICB9CiAgICAgICAgICB9CiAgICAgICAgfQogICAgICBdLAogICAgICAiZG9jdW1lbnQiOiBbCiAgICAgICAgewogICAgICAgICAgInR5cGUiOiAiRGlnaXRhbERvY3VtZW50IiwKICAgICAgICAgICJuYW1lIjogIlNhbGVzIENvbnRyYWN0IiwKICAgICAgICAgICJlbmNvZGluZ0Zvcm1hdCI6ICJhcHBsaWNhdGlvbi96aXAiLAogICAgICAgICAgImFib3V0IjogewogICAgICAgICAgICAidHlwZSI6ICJUcmFuc2FjdGlvbiIsCiAgICAgICAgICAgICJpZGVudGlmaWVyIjogewogICAgICAgICAgICAgICJndXJ1VHJhbnNhY3Rpb25JZCI6ICIwMDAwMDc0NzkyIgogICAgICAgICAgICB9CiAgICAgICAgICB9LAogICAgICAgICAgInVybCI6ICJodHRwczovL2V4YW1wbGUuY29tL3BhdGgvdG8vZG9jdW1lbnQucGRmIgogICAgICAgIH0KICAgICAgXQogICAgfSwKICAgICJpbnN0cnVtZW50IjogewogICAgICAidHlwZSI6ICJTb2Z0d2FyZUFwcGxpY2F0aW9uIiwKICAgICAgIm5hbWUiOiAiQnJlYWtpbmcgQk1TIgogICAgfQogIH0KfQ","isBase64Encoded": true},"agent": "https://dave.bhhs.hsfaffiliates.com/profile/card#me","instrument": "https://dave.bhhs.hsfaffiliates.com/profile/card#me","scope": [],"policy": {},"stage": "request","hasData": true,"contentType": "application/json","object": {"topic": "realestate/franchise#transactionreport","source": "https://dave.bhhs.dev.yodata.io/profile/card#me","recipient": "https://mi301.bhhs.dev.yodata.io/profile/card#me","data": {"type": "UpdateAction","object": {"type": "RealEstateTransaction","identifier": {"guruTransactionId": "0000074792"},"additionalProperty": {"batchid": "4136","bhhsTransactionSequence": 1,"bhhsUnimprovedLandFlag": true},"transactionStatus": "ClosedTransactionStatus","transactionType": "ST","reportingOffice": "https://{officeid}.example.com/profile/card#me","listingOffice": "https://{officeid}.example.com/profile/card#me","buyerOffice": "https://{officeid}.example.com/profile/card#me","commissionDate": "2020-08-14T03:19:08Z","closeDate": "2020-08-14T03:19:08Z","purchaseContractDate": "2020-08-14T03:19:08Z","closePrice": {"type": "MonetaryAmount","value": 123456.78,"currency": "USD"},"totalSalesProductionGCI": {"type": "MonetaryAmount","value": 123456.78,"currency": "USD"},"totalSalesProductionGCIDeduction": {"type": "MonetaryAmount","value": 1234.56,"currency": "USD"},"object": {"type": "RealEstateProperty","propertyType": "RESI","propertySubType": "SingleFamilyPropertyType","streetAddress": "1433 W 150 N","addressLocality": "Springville","addressRegion": "UT","postalCode": "84663","addressCountry": "USA","universalPropertyId": "US-04015-N-R-11022331-N"},"referral": {"type": "Referral","referredBy": {"type": "RealEstateOrganization","id": "https://FL305.bhhs.hsfaffiliates.com/profile/card#me"},"additionalProperty": {"isReferralYN": "Y","bhssInNetworkReferralYN": "Y"}},"participant": [{"type": "TransactionParticipant","roleName": "Buyer","position": 1,"givenName": "Josh","familyName": "Cold","additionalName": "Stone","email": "joe@example.com","telephone": "8885550001","affiliation": ["https://474912.bhhs.hsfaffiliates.com/profile/card#me"]}],"transactionEntry": [{"type": "TransactionEntry","salesProductionUnit": 0.5,"salesProductionGCI": {"type": "MonetaryAmount","value": 1234.56,"currency": "USD"},"recipient": {"type": "RealEstateAgent","roleName": "ListingAgent","id": "https://{agentid}.bhhs.hsfaffiliates.com/profile/card#me","identifier": {"guruAgentId": "218034"}}}],"document": [{"type": "DigitalDocument","name": "Sales Contract","encodingFormat": "application/zip","about": {"type": "Transaction","identifier": {"guruTransactionId": "0000074792"}},"url": "https://example.com/path/to/document.pdf"}]},"instrument": {"type": "SoftwareApplication","name": "Breaking BMS"}}}}
		const result = await validateSchema(event);
		return expect(result).toEqual(event)
	})
})

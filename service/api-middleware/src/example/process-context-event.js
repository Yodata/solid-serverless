module.exports = {
  "request": {
    "method": "POST",
    "url": "https://profile-events.bhhs.hsfaffiliates.com/publish/",
    "body": "ewogICJAY29udGV4dCIgOiAiaHR0cHM6Ly9wcm9maWxlLWV2ZW50cy5iaGhzLmhzZmFmZmlsaWF0ZXMuY29tL3B1YmxpYy9jb250ZXh0L3N0YWdlL3JlYWwtZXN0YXRlLWNvbXBhbnkuY2RlZi55YW1sIiwKICAiZGF0YSIgOiB7CiAgICAidHlwZSIgOiAiVXBkYXRlQWN0aW9uIiwKICAgICJwb2QiIDogInByb2ZpbGUtZXZlbnRzLmJoaHMuaHNmYWZmaWxpYXRlcy5jb20iLAogICAgIk9iamVjdCIgOiAiUmVhbEVzdGF0ZU9yZ2FuaXphdGlvbiIsCiAgICAiQWNjb3VudCIKICAgOiB7ImF0dHJpYnV0ZXMiOnsidHlwZSI6IkFjY291bnQiLCJ1cmwiOiIvc2VydmljZXMvZGF0YS92NDYuMC9zb2JqZWN0cy9BY2NvdW50LzAwMTRCMDAwMDBjdVVlWlFBVSJ9LCJJc0RlbGV0ZWQiOmZhbHNlLCJCaWxsaW5nQ2l0eSI6IkZyZWRlcmlja3NidXJnIiwiUHJpbWFyeV9QaG9uZV9fYyI6Iig1NDApIDM3MS04MDA5IiwiQmlsbGluZ0NvdW50cnkiOiJVbml0ZWQgU3RhdGVzIiwiQmlsbGluZ1N0cmVldCI6IjMxMDYgUGxhbmsgUmQuIiwiQmlsbGluZ1N0YXRlQ29kZSI6IlZBIiwiQmlsbGluZ0NvdW50cnlDb2RlIjoiVVMiLCJCaWxsaW5nU3RhdGUiOiJWaXJnaW5pYSIsIlN0YXR1c19fYyI6IkFjdGl2ZSIsIkZyYW5jaGlzZV9Db21taXRtZW50X19jIjoiT3B0ZWQgT3V0IiwiTmFtZSI6IlNlbGVjdCBSZWFsdHkiLCJCaWxsaW5nQWRkcmVzcyI6eyJjaXR5IjoiRnJlZGVyaWNrc2J1cmciLCJjb3VudHJ5IjoiVW5pdGVkIFN0YXRlcyIsImNvdW50cnlDb2RlIjoiVVMiLCJnZW9jb2RlQWNjdXJhY3kiOiJBZGRyZXNzIiwibGF0aXR1ZGUiOjM4LjI5MzY5NCwibG9uZ2l0dWRlIjotNzcuNTE3MTYyLCJwb3N0YWxDb2RlIjoiMjI0MDciLCJzdGF0ZSI6IlZpcmdpbmlhIiwic3RhdGVDb2RlIjoiVkEiLCJzdHJlZXQiOiIzMTA2IFBsYW5rIFJkLiJ9LCJGYXgiOiIoNTQwKSAzNzEtNzY1NiIsIlByaW1hcnlfRW1haWxfX2MiOiJkYW5AZ2V0c29sZHZhLmNvbSIsIkNvbnN1bWVyX0VtYWlsX19jIjoic2FsZXNAZ2V0c29sZHZhLmNvbSIsIklkIjoiMDAxNEIwMDAwMGN1VWVaUUFVIiwiSFNGX0lEX19jIjoiVkEzMDYiLCJJc19EaXNwbGF5ZWRfX2MiOiJZZXMiLCJCaWxsaW5nUG9zdGFsQ29kZSI6IjIyNDA3IiwiQ29uc3VtZXJfUGhvbmVfX2MiOiIoODc3KSA3NjUtMzIxNCIsIklzX1ByaW1hcnlfT2ZmaWNlX19jIjpmYWxzZSwiUmVjb3JkVHlwZUlkIjoiMDEyNjEwMDAwMDA3SUhCQUEyIiwiUmVsb2NhdGlvbl9XZWJzaXRlX19jIjpudWxsLCJQYXJlbnRJZCI6bnVsbCwiRnJhbmNoaXNlX0NvZGVfX2MiOm51bGwsIlByaW1hcnlfV2Vic2l0ZV9fYyI6bnVsbCwiUmVsb2NhdGlvbl9QaG9uZV9fYyI6bnVsbCwiT2ZmaWNlX3R5cGVfX2MiOm51bGwsIlJlbG9jYXRpb25fRmF4X19jIjpudWxsLCJPZmZpY2VfQ29udGFjdF9fYyI6bnVsbCwiVG9sbF9GcmVlX19jIjpudWxsLCJSZWxvY2F0aW9uX1RvbGxfRnJlZV9fYyI6bnVsbH19IH0",
    "isBase64Encoded": true
  },
  "agent": "https://profile-events.bhhs.hsfaffiliates.com/profile/card#me",
  "instrument": "https://profile-events.bhhs.hsfaffiliates.com/profile/card#me",
  "scope": [],
  "policy": {},
  "stage": "request",
  "hasData": true,
  "object": {
    "@context": "https://profile-events.bhhs.hsfaffiliates.com/public/context/stage/real-estate-company.cdef.yaml",
    "data": {
      "type": "UpdateAction",
      "pod": "profile-events.bhhs.hsfaffiliates.com",
      "Object": "RealEstateOrganization",
      "Account": {
        "attributes": {
          "type": "Account",
          "url": "/services/data/v46.0/sobjects/Account/0014B00000cuUeZQAU"
        },
        "IsDeleted": false,
        "BillingCity": "Fredericksburg",
        "Primary_Phone__c": "(540) 371-8009",
        "BillingCountry": "United States",
        "BillingStreet": "3106 Plank Rd.",
        "BillingStateCode": "VA",
        "BillingCountryCode": "US",
        "BillingState": "Virginia",
        "Status__c": "Active",
        "Franchise_Commitment__c": "Opted Out",
        "Name": "Select Realty",
        "BillingAddress": {
          "city": "Fredericksburg",
          "country": "United States",
          "countryCode": "US",
          "geocodeAccuracy": "Address",
          "latitude": 38.293694,
          "longitude": -77.517162,
          "postalCode": "22407",
          "state": "Virginia",
          "stateCode": "VA",
          "street": "3106 Plank Rd."
        },
        "Fax": "(540) 371-7656",
        "Primary_Email__c": "dan@getsoldva.com",
        "Consumer_Email__c": "sales@getsoldva.com",
        "Id": "0014B00000cuUeZQAU",
        "HSF_ID__c": "VA306",
        "Is_Displayed__c": "Yes",
        "BillingPostalCode": "22407",
        "Consumer_Phone__c": "(877) 765-3214",
        "Is_Primary_Office__c": false,
        "RecordTypeId": "012610000007IHBAA2",
        "Relocation_Website__c": null,
        "ParentId": null,
        "Franchise_Code__c": null,
        "Primary_Website__c": null,
        "Relocation_Phone__c": null,
        "Office_type__c": null,
        "Relocation_Fax__c": null,
        "Office_Contact__c": null,
        "Toll_Free__c": null,
        "Relocation_Toll_Free__c": null
      }
    }
  }
}
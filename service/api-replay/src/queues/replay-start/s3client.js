const { S3Client } = require('@aws-sdk/client-s3')
const config = require('./service-config')
const region = process.env.AWS_REGION || config.AWS_REGION || 'us-west-2'
const s3Client = new S3Client({ region })

module.exports = s3Client

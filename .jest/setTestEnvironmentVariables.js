module.exports = async function setupTestEnvironment() {
  process.env.SOLID_HOST = 'bhhs.dev.yodata.io'
  process.env.SOLID_STORE = 'yodata-dev-solid-serverless-storage'
  process.env.AWS_REGION = 'us-west-2'
  process.env.SVC_KEY = process.env.SVC_KEY
}
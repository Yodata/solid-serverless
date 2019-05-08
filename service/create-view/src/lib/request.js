const yaml = require('js-yaml')
const got = require('got')
const chalk = require('chalk')
const client = got.extend({
  // baseUrl: get('pod.url'),
  headers: {
    'user-agent': `yodata/client (https://yodata.io)`,
    // 'x-api-key': get('pod.secret')
  },
  hooks: {
    beforeRequest: [
      logRequest
    ],
    afterResponse: [
      parseResponseData
    ]
  }
})

module.exports = client

function logRequest(request) {
  console.info(`${chalk.blue(request.method)} ${chalk.green(request.href)}`)
  return request
}

async function parseResponseData(response) {
  const contentType = String(response.headers['content-type'])
  if (contentType.includes('json')) {
    response.data = JSON.parse(response.body)
  } else if (contentType.includes('yaml')) {
    response.data = yaml.load(response.body)
  }
  return response
}

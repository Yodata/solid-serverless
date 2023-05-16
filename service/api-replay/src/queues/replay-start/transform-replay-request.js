// ## input parameters
// ```yaml
// type: ReplayStartAction
// target: 'https://example.com/inbox/'
// startDate: '2022-09-30T02:09:16.483Z'
// endDate: '2022-09-30T03:09:16.483Z'
// ```

// ## transformed input parameters
// ```yaml
// type: ReplayStartAction
// bucket: process.env.SOLID_STORE
// prefix: entities/${target.host}/data/by-ts/${target.path}/year/month/day/
// startSuffix: year/month/day/hours/minutes/seconds/ms/
// endSuffix: year/month/day/hours/minutes/ms/
// ```

const { SOLID_STORE } = require("./service-config");
const getPathFromDate = require("./get-path-from-date");
const getPathFromTarget = require("./get-path-from-target");
const getCommonPrefix = require("./get-common-prefix");

async function transformReplayRequest(input) {
  const { target, startDate, endDate, items, filter = {} } = input;
  const bucket = SOLID_STORE;
  const targetPath = getPathFromTarget(target);
  const startPath = targetPath + getPathFromDate(startDate);
  const endPath = targetPath + getPathFromDate(endDate);
  const prefix = getCommonPrefix(startPath, endPath);
  return {
    type: "ReplayStartAction",
    target,
    bucket,
    prefix,
    startPath,
    endPath,
    items,
    filter,
  };
}

module.exports = transformReplayRequest;

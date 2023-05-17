// Import required AWS SDK clients and commands for Node.js.
const arc = require("@architect/functions");
const {
  ListObjectsV2Command,
  GetObjectCommand,
} = require("@aws-sdk/client-s3");
const s3Client = require("./s3client.js");
const logger = require("@yodata/logger");
const {
  REPLAY_ITEM_LIMIT,
  REPLAY_BATCH_SIZE,
  SOLID_STORE: bucket,
} = require("./service-config");

function getItemId(item) {
  return item.Key.split("/").slice(-1)[0];
}

function createItemReducer(endPath) {
  return (result, item, index, initialValue) => {
    if (!Array.isArray(result)) result = initialValue;
    if (item.Key < endPath) {
      result.push(getItemId(item));
    }
    return result;
  };
}
function isPlainObject(value) {
  if (Object.prototype.toString.call(value) !== "[object Object]") {
    return false;
  }
  const prototype = Object.getPrototypeOf(value);
  return prototype === null || prototype === Object.prototype;
}
function flatten(obj) {
  const result = {};

  function traverse(currentObj, parentKey) {
    for (const key in currentObj) {
      const value = currentObj[key];
      const currentKey = parentKey ? `${parentKey}.${key}` : key;

      if (isPlainObject(value)) {
        traverse(value, currentKey);
      } else {
        if (!result.hasOwnProperty(key)) {
          result[key] = value;
        }
      }
      if (obj.hasOwnProperty(key)) {
        result[key] = obj[key];
      }
    }
  }

  traverse(obj);

  return result;
}
async function convertToJson(body) {
  const dataBuffer = [];

  return new Promise((resolve, reject) => {
    body.on("data", (chunk) => {
      dataBuffer.push(chunk);
    });
    body.on("end", () => {
      const data = Buffer.concat(dataBuffer).toString("utf-8");
      const jsonData = JSON.parse(data);
      resolve(jsonData || {});
    });
    body.on("error", (error) => {
      reject(error);
    });
  });
}
async function getDataFrom_by_id(itemsArray, filters, target) {
  try {
    const podUrl = target.split("/");
    const getObjectPromises = itemsArray?.map((_) => {
      return s3Client.send(
        new GetObjectCommand({
          Bucket: bucket,
          Key: `entities/${podUrl[2]}/data/by-id/inbox/${_}`,
        })
      );
    });
    const all_JSON = await Promise.all(getObjectPromises)
      .then(async (data) => {
        const results = data.map(async (obj) => {
          const body = obj.Body;
          return await convertToJson(body);
        });
        return Promise.all(results);
      })
      .catch((err) => {
        return err;
      });
    const json_Array = await Promise.all(all_JSON);
    const filteredObjects = json_Array?.filter((object) => {
      const flattened = flatten(object);
      for (const [key, value] of Object.entries(filters)) {
        const flattenedValue = flattened[key];
        if (
          typeof flattenedValue == "string" &&
          flattenedValue.includes(value)
        ) {
          continue;
        }
        return false;
      }
      return object;
    });
    const filteredKeys = filteredObjects.map(
      (_) => _[`@id`].split("/")[_[`@id`].split("/").length - 1]
    );
    return filteredKeys;
  } catch (error) {}
}

async function publishItems(target, items, filter = {}) {
  const filterItems = await getDataFrom_by_id(items, filter, target);
  return arc.queues
    .publish({
      name: "replay-items",
      payload: {
        target,
        items: filterItems,
      },
    })
    .then((result) => {
      logger.debug("PUBLISHED_ITEMS", {
        target,
        items,
        result,
      });
      return { target, items, result };
    })
    .catch((error) => {
      logger.error("PUBLISH_ITEM_ERROR", { target, items, error });
      return {
        target,
        items,
        error: { message: error.message, stack: error.stack },
      };
    });
}
function filterRequiredItems(requiredItems, allItems) {
  const filteredArray =
    requiredItems.filter((item) => allItems.includes(item)) || [];
  return filteredArray;
}
/**
 * gets replay list from store (S3 bucket) &
 * publishes replay items commands to replay-items queue (1000 item batches)
 * @function processReplayRequest
 * @param {object} input
 * @param {string} input.target - the container url to be replayed
 * @param {string} input.bucket - the bucket where the replay items are stored
 * @param {string} input.prefix - lowest level path containing all replay items
 * @param {string} input.startPath - the path to start replaying from
 * @param {string} input.endPath - the path to end replaying from
 * !
 */
async function processReplayRequest(input) {
  const {
    target,
    bucket,
    prefix,
    startPath,
    endPath,
    items: bodyItems = [],
    filter = {},
  } = input;
  const reducer = createItemReducer(endPath);
  const commandOptions = {
    Bucket: bucket,
    Prefix: prefix,
    StartAfter: startPath,
    MaxKeys: REPLAY_BATCH_SIZE,
  };

  let replayCompleted = false; // true when one or more items in response are greater than the endPrefix
  let lastKey = startPath;
  let itemsPublished = 0;
  let statusMessage = "";

  while (!replayCompleted) {
    try {
      logger.debug("ListObjectsV2Command", commandOptions);
      const response = await s3Client.send(
        new ListObjectsV2Command(commandOptions)
      );
      if (Number(response.KeyCount) > 0) {
        const items = response.Contents.reduce(reducer, []);

        const filteredItems =
          bodyItems.length > 0
            ? filterRequiredItems(items, bodyItems)
            : [...items];
        await publishItems(target, filteredItems, filter);
        itemsPublished += items.length;
        lastKey = response.Contents.slice(-1)[0].Key;
      }
      if (lastKey > endPath) {
        replayCompleted = true;
        statusMessage = "stopped after end date";
      }
      if (itemsPublished >= REPLAY_ITEM_LIMIT) {
        replayCompleted = true;
        statusMessage = `replay limit (${REPLAY_ITEM_LIMIT}) reached`;
      }
      if (!response.IsTruncated) {
        replayCompleted = true;
        if (response.KeyCount === 0 && itemsPublished === 0) {
          statusMessage = "no items found to replay - check your input";
        } else {
          statusMessage = "end of container reached";
        }
      }
      if (!replayCompleted) {
        commandOptions.StartAfter = lastKey;
        logger.debug("REPLAY_STATUS", {
          target,
          itemsPublished,
          lastKey,
        });
      }
    } catch (err) {
      replayCompleted = true;
      throw new Error(err.message);
    }
  }
  return `queued ${itemsPublished} items to replay on ${target}, ${statusMessage}`;
}

exports.processReplayRequest = processReplayRequest;
exports.createItemReducer = createItemReducer;
exports.getItemId = getItemId;

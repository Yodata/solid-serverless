const DEFAULT_CONTEXT = 'https://yodata.me/public/real_estate/context#'
const AUTHORIZATION = 'Authorization'

/** @enum ACTION_STATUS */
const ACTION_STATUS = {
    COMPLETED: 'CompletedActionStatus',
    FAILED: 'FailedActionStatus',
    ACTIVE: 'ActiveActionStatus',
    PENDING: 'PendingActionStatus'
}

module.exports = {
    DEFAULT_CONTEXT,
    AUTHORIZATION,
    ACTION_STATUS
}
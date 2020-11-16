const ERROR = 'error'
const DEBUG = 'debug'
const WARN = 'warn'

const expectedEnvironmentVariabls =
{
	APPLY_POLICY_FUNCTION_NAME: ERROR,
	BMS_TRANSACTION_FUNCTION_NAME: ERROR,
	CHECK_SCOPE_FUNCTION_NAME: ERROR,
	CREATE_SFDC_CONTACT_FUNCTION_NAME: ERROR,
	CREATE_VIEW_FUNCTION_NAME: ERROR,
	DATA_POLICY_PATH: ERROR,
	DATA_POLICY_SVC_HOST: ERROR,
	DATA_POLICY_WL: WARN,
	DEFAULT_JSONLD_CONTEXT: WARN,
	LOG_LEVEL: WARN,
	SOLID_HOST: ERROR,
	VALIDATE_SCHEMA_FUNCTION_NAME: ERROR,
}

const checkenv = (envmap = expectedEnvironmentVariabls) => {
	Object.entries(envmap).forEach(([ name, level ]) => {
		const value = process.env[ name ]
		if (typeof value !== 'string' || value.length === 0) {
			switch (level) {
			case DEBUG:
				console.debug(`environment var ${name} is not set`)
				break
			case WARN:
				console.warn(`environment var ${name} is not set`)
				break
			case ERROR:
				console.error(`required environment var ${name} is not set`)
				throw new Error(`required environment var ${name} is not set`)
			default:
				console.error(`environment var ${name} is not set`)
				throw new Error(`check-env value ${level} was not recognized`)
			}
		}
	})
}


module.exports = checkenv
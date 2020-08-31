# Deployment Process
    * Set profile in app.arc 
    * Run command => arc deploy
# Run Test
    * cd schema-validator/
    * Run command => npm test
# Request Format
    {
        event: {},
        config: {
            schemaURL
        }
    }
# Resonse Format
    {   
        "isValid": true
    }
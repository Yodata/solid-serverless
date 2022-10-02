@app
api-replay

@http
post /replay

@queues
replay-start
replay-items

@aws
# profile default
runtime nodejs16.x
region us-west-2
architecture arm64

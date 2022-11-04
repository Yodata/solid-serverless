@app
api-publish2

@http
post /publish2
get /publish2/status

@queues
publish2-event

@aws
# profile default
architecture arm64
fifo false
region us-west-2
runtime nodejs16.x

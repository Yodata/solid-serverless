package io.yodata.ldp.solid.server.aws.event;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.aws.transform.AWSTransformService;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericProcessor {

    private final Logger log = LoggerFactory.getLogger(GenericProcessor.class);

    private AmazonSQS sqs;
    private AWSLambda lambda;

    private S3Store store;
    private ContainerHandler storeHandler;
    private AWSTransformService transform;

    public GenericProcessor() {
        this.store = S3Store.getDefault();
        this.storeHandler = new ContainerHandler(store);
        this.transform = new AWSTransformService();

        this.sqs = AmazonSQSClientBuilder.defaultClient();
        this.lambda = AWSLambdaClientBuilder.defaultClient();
    }

    public void handleEvent(JsonObject event) {
        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        URI id = URI.create(action.getId());
        URI target = URI.create(action.getTarget());
        log.info("Processing storage event {} about {}", action.getType(), action.getId());

        List<Subscription> subs = store.getSubscriptions(id);
        if (subs.isEmpty()) {
            log.info("No subscription found");
            return;
        }
        log.info("Processing {} subscription(s)", subs.size());

        for (Subscription sub : subs) {
            log.info("Processing subscription ID {} on target {}", sub.getId(), target.getPath());
            if (!target.getPath().startsWith(sub.getObject())) {
                log.info("Subscription ID {} does not match the object, skipping", sub.getId());
                continue;
            }

            if (Objects.nonNull(sub.getScope())) {
                log.info("Subscription has a scope, processing");
                JsonObject rawData = action.getObject().orElseGet(() -> {
                    log.info("Object is not present in the event: Fetching data from store to process");
                    Optional<S3Object> obj = store.getEntityFile(id);
                    if (!obj.isPresent()) {
                        log.info("We got a notification about {} which doesn't exist anymore, skipping filtering", id);
                        return new JsonObject();
                    } else {
                        return GsonUtil.parseObj(obj.get().getObjectContent());
                    }
                });
                TransformMessage msg = new TransformMessage();
                msg.setSecurity(action.getRequest().getSecurity());
                msg.setScope(action.getRequest().getScope());
                msg.setPolicy(store.getPolicies(id));
                msg.setObject(rawData);
                rawData = transform.transform(msg);
                if (rawData.keySet().isEmpty()) {
                    log.info("Transform removed all data, not sending notification");
                    continue;
                }

                action.setObject(rawData);
            }
            log.info("Data after scope: {}", GsonUtil.toJson(action));

            if (!StringUtils.isEmpty(sub.getAgent())) {
                log.info("Subscription is external, we'll send to outbox");

                if (!action.getObject().isPresent()) {
                    log.info("No content to send");
                    continue;
                }

                // We rebuild the event with only the fields we want
                JsonObject notification = new JsonObject();
                notification.addProperty(ActionPropertyKey.Type.getId(), action.getType());
                notification.addProperty(ActionPropertyKey.Id.getId(), action.getId());
                notification.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());

                // We add the relevant agent and instrument info from the request
                action.getRequest().getSecurity().getAgent()
                        .ifPresent(agent -> notification.addProperty(ActionPropertyKey.Agent.getId(), agent));
                notification.addProperty(ActionPropertyKey.Instrument.getId(), action.getRequest().getSecurity().getInstrument());

                // We add the data type
                notification.addProperty(ActionPropertyKey.Target.getId(), id.toString());
                action.getObject().ifPresent(obj -> notification.add(ActionPropertyKey.Object.getId(), obj));

                // We add the audience
                notification.addProperty("@to", sub.getAgent());

                // We build the store request
                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(new Target(id), "/outbox/"));
                r.setBody(notification);

                // We send to store
                Response res = storeHandler.post(r);
                String eventId = GsonUtil.parseObj(res.getBody()
                        .orElse("{\"id\":\"<NOT RETURNED>\"".getBytes(StandardCharsets.UTF_8))).get("id").getAsString();
                log.info("Data was saved at {}", eventId);
            } else {
                log.info("Subscription is internal, sending full version to endpoint");
                send(GsonUtil.get().toJsonTree(action).getAsJsonObject(), sub.getTarget());
            }
        }

        log.info("All subscriptions processed");
    }

    public void send(JsonObject data, String targetRaw) {
        try {
            URI target = URI.create(targetRaw);
            if (StringUtils.equals("aws-sqs", target.getScheme())) {
                String queueUrl = new URIBuilder(target).setScheme("https").build().toURL().toString();
                sqs.sendMessage(queueUrl, GsonUtil.toJson(data));
                log.info("Event dispatched to SQS queue {}", queueUrl);
            } else if (StringUtils.equals(target.getScheme(), "aws-lambda")) {
                String lName = target.getAuthority();
                InvokeRequest i = new InvokeRequest();
                i.setFunctionName(lName);
                i.setPayload(GsonUtil.toJson(data));
                InvokeResult r = lambda.invoke(i);
                int statusCode = r.getStatusCode();
                String functionError = r.getFunctionError();
                if (statusCode != 200 || StringUtils.isNotEmpty(functionError)) {
                    throw new RuntimeException("Error when calling lambda " + lName + " | Status code: " + statusCode + " | Error: " + functionError);
                }
                log.info("Lambda {} was successfully called", lName);
            } else if (StringUtils.equals(target.getScheme(), "aws-s3")) {
                throw new RuntimeException("AWS S3 is not implemented as subscription target");
            } else if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                throw new RuntimeException("HTTP is not implemented as subscription target");
            } else {
                throw new RuntimeException("Scheme " + target.getScheme() + " is not supported");
            }
        } catch (IllegalArgumentException e) {
            log.error("Target destination is not understood: {}", targetRaw);
        } catch (URISyntaxException e) {
            log.error("Cannot build URI", e);
        } catch (MalformedURLException e) {
            log.error("Cannot build URL", e);
        }
    }

}

/*
 * Copyright 2019 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata.ldp.solid.server.model.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.SolidPod;
import io.yodata.ldp.solid.server.model.SolidSession;
import io.yodata.ldp.solid.server.model.data.*;
import io.yodata.ldp.solid.server.model.env.Environment;
import io.yodata.ldp.solid.server.model.processor.RequestFilter;
import io.yodata.ldp.solid.server.model.processor.ResponseFilter;
import io.yodata.ldp.solid.server.model.store.PodStore;
import io.yodata.ldp.solid.server.model.store.fs.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BasicPod implements SolidPod {

    private static final Logger log = LoggerFactory.getLogger(BasicPod.class);

    private final String id;
    private final SecurityContext creds;
    private final Environment env;
    private final BasicAuth auth;

    public BasicPod(String id, Environment env) {
        this.id = id;
        this.env = env;
        this.creds = SecurityContext.forPod(id);
        this.auth = new BasicAuth(id, env.getStore());
    }

    private PodStore getStore() {
        return env.getStore().forPod(id);
    }

    private Response build(FsElementMeta meta) {
        Response r = new Response();
        r.getHeaders().put("Content-Type", meta.getContentType());
        r.getHeaders().put("Content-Length", Long.toString(meta.getLength()));
        r.getHeaders().putAll(meta.getProperties());
        return r;
    }

    protected Exchange build(Request in) {
        Target t = Objects.requireNonNull(in.getTarget());
        in.setPolicy(env.getStore().getPolicies(t.getId()));
        Exchange ex = new Exchange();
        ex.setRequest(in);
        return ex;
    }

    @Override
    public SecurityContext getIdentity() {
        return creds;
    }

    @Override
    public SecurityContext identifyWithApiKey(String apiKey) {
        return auth.authenticate(apiKey);
    }

    @Override
    public SolidSession getSession(SecurityContext sc) {
        return new BasicSession();
    }

    private Response run(Request req, BiConsumer<RequestFilter, Exchange> filterIn, BiConsumer<ResponseFilter, Exchange> filterOut, Function<Request, Response> toDo) {
        Exchange ex = build(req);

        for (RequestFilter filter : env.getInputFilters()) {
            filterIn.accept(filter, ex);
            if (Objects.nonNull(ex.getResponse())) {
                return ex.getResponse();
            }
        }

        Response r = toDo.apply(ex.getRequest());
        ex.setResponse(r);

        for (ResponseFilter filter : env.getOutputFilters()) {
            filterOut.accept(filter, ex);
            r = filter.get(ex);
        }

        return r;
    }

    @Override
    public Response head(Request req) {
        log.info("Getting Resource meta {}", req.getTarget().getPath());

        return run(req, RequestFilter::head, ResponseFilter::head, req1 -> {
            FsElementMeta meta = getStore().head(req.getTarget().getPath());
            return build(meta);
        });
    }

    @Override
    public Response get(Request req) {
        log.info("Getting Resource {}", req.getTarget().getPath());

        return run(req, RequestFilter::get, ResponseFilter::get, req1 -> {
            try {
                FsElement el = getStore().get(req.getTarget().getPath());

                Response r = build(el.getMeta());
                r.setBody(IOUtils.toByteArray(el.getData(), el.getMeta().getLength()));
                return r;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public Page list(Target t, String by, String from, boolean isFullFormat, boolean isTemporal) {
        Page page = new Page();
        FsPage fsPage = getStore().list(t.getPath());

        page.setNext(fsPage.getNext());
        fsPage.getElements().forEach(path -> {
            if (isFullFormat) {
                try {
                    JsonElement el = GsonUtil.parseEl(getStore().get(Paths.get(t.getPath(), path)).getData());
                    page.getContains().add(el);
                } catch (RuntimeException e) {
                    throw new EncodingNotSupportedException("Cannot create listing: Invalid JSON object at " + path);
                }
            } else {
                page.getContains().add(new JsonPrimitive("https://" + id + t.getPath() + "/" + path));
            }
        });

        return page;
    }

    @Override
    public Response post(Request in) {
        put(in); // FIXME this is not right!

        // FIXME return something useful
        return new Response();
    }

    @Override
    public Response put(Request in) {
        BasicMeta meta = new BasicMeta();
        meta.setContentType(in.getContentType().orElseThrow(() -> new IllegalArgumentException("Content type must be given")));
        meta.setLength(in.getBody().length);
        BasicElement el = new BasicElement(meta, new ByteArrayInputStream(in.getBody()));
        boolean saved = getStore().save(in.getDestination().getPath(), el);

        // FIXME return something useful
        Response r = new Response();
        r.setStatus(saved ? 204 : 201);
        return r;
    }

    @Override
    public Response delete(Request in) {
        getStore().delete(in.getDestination().getPath());
        // FIXME return something useful
        return new Response();
    }

}

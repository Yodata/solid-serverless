/*
 * Solid Serverless
 * Copyright 2018 YoData, Inc.
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
import io.yodata.ldp.solid.server.exception.UnauthenticatedException;
import io.yodata.ldp.solid.server.model.Environment;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.SolidPod;
import io.yodata.ldp.solid.server.model.SolidSession;
import io.yodata.ldp.solid.server.model.data.Page;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.model.data.Target;
import io.yodata.ldp.solid.server.model.store.PodStore;
import io.yodata.ldp.solid.server.model.store.fs.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

public class BasicPod implements SolidPod {

    private static final Logger log = LoggerFactory.getLogger(BasicPod.class);

    private final String id;
    private final SecurityContext creds;
    private final Environment env;

    public BasicPod(String id, Environment env) {
        this.id = id;
        this.env = env;
        this.creds = SecurityContext.forPod(id);
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

    @Override
    public SecurityContext getIdentity() {
        return creds;
    }

    @Override
    public SecurityContext identifyWithApiKey(String apiKey) {
        String apiKeyPath = "/security/api/key/" + apiKey;

        FsElement el = getStore().tryGet(apiKeyPath)
                .or(() -> env.getStore().forGlobal().tryGet(apiKeyPath))
                .orElseThrow(UnauthenticatedException::new);

        return GsonUtil.parse(el.getData(), SecurityContext.class);
    }

    @Override
    public SolidSession getSession(SecurityContext sc) {
        return new BasicSession();
    }

    @Override
    public Response head(Target target) {
        log.info("Getting Resource meta {}", target.getPath());

        FsElementMeta meta = getStore().head(target.getPath());
        return build(meta);
    }

    @Override
    public Response get(Target target) {
        log.info("Getting Resource {}", target.getPath());

        try {
            FsElement el = getStore().get(target.getPath());

            Response r = build(el.getMeta());
            r.setBody(IOUtils.toByteArray(el.getData(), el.getMeta().getLength()));

            return r;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
    public void post(Request in) {
        save(in);
    }

    @Override
    public boolean save(Request in) {
        BasicMeta meta = new BasicMeta();
        meta.setContentType(in.getContentType().orElseThrow(() -> new IllegalArgumentException("Content type must be given")));
        meta.setLength(in.getBody().length);
        BasicElement el = new BasicElement(meta, new ByteArrayInputStream(in.getBody()));
        return getStore().save(in.getDestination().getPath(), el);
    }

    @Override
    public void delete(Request in) {
        getStore().delete(in.getDestination().getPath());
    }

}

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

package io.yodata.ldp.solid.server.model.event;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class EventTopic {

    public static final String DELIMITER = "#";

    private String path = "";
    private String action = "";

    public static EventTopic parse(String raw) {
        if (StringUtils.isBlank(raw)) {
            throw new IllegalArgumentException("A topic cannot be blank");
        }

        EventTopic topic = new EventTopic();

        String[] elements = raw.split(DELIMITER, 2);
        topic.path = elements[0];
        topic.action = StringUtils.defaultIfEmpty(elements[1], "");

        if (StringUtils.isEmpty(topic.path)) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        return topic;
    }

    public static Optional<EventTopic> tryParse(String raw) {
        try {
            return Optional.of(parse(raw));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private EventTopic() {
        // only for static constructor
    }

    public EventTopic(String path, String action) {
        this.path = path;
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public String getAction() {
        return action;
    }

    public String full() {
        return toString();
    }

    @Override
    public String toString() {
        return path + DELIMITER + action;
    }

}

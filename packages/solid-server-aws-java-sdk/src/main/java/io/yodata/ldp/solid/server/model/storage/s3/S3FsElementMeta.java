package io.yodata.ldp.solid.server.model.storage.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class S3FsElementMeta implements FsElementMeta {

    private String contentType;
    private long length;
    private Map<String, String> properties;
    private boolean isLink;

    S3FsElementMeta(ObjectMetadata s3Meta) {
        contentType = s3Meta.getContentType();
        length = s3Meta.getContentLength();
        properties = new HashMap<>(s3Meta.getUserMetadata());
        isLink = StringUtils.isNotBlank(s3Meta.getUserMetaDataOf("Solid-Serverless-Link"));
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean isLink() {
        return isLink;
    }

}

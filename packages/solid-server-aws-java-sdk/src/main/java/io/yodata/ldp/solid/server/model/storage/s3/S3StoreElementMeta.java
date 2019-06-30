package io.yodata.ldp.solid.server.model.storage.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import io.yodata.ldp.solid.server.model.storage.StoreElementMeta;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class S3StoreElementMeta implements StoreElementMeta {

    protected String contentType;
    protected long length;
    protected Map<String, String> properties;
    protected boolean isLink;

    public S3StoreElementMeta(ObjectMetadata s3Meta) {
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

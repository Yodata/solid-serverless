package io.yodata.ldp.solid.server.model.storage.s3;

import com.amazonaws.services.s3.model.S3Object;
import io.yodata.ldp.solid.server.model.storage.StoreElement;

import java.io.InputStream;

public class S3StoreElement extends S3StoreElementMeta implements StoreElement {

    private S3Object obj;

    public S3StoreElement(S3Object obj) {
        super(obj.getObjectMetadata());
        this.obj = obj;
    }

    @Override
    public InputStream getData() {
        return obj.getObjectContent();
    }

}

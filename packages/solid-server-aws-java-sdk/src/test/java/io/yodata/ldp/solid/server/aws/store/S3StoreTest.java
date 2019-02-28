package io.yodata.ldp.solid.server.aws.store;

import org.junit.BeforeClass;

public class S3StoreTest {

    private static S3Store store;

    @BeforeClass
    public void beforeClass() {
        store = S3Store.getDefault();
    }



}

package io.yodata.test.ldp.solid.server.model.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import io.yodata.EnvUtils;
import io.yodata.ldp.solid.server.model.storage.Store;
import io.yodata.ldp.solid.server.model.storage.s3.S3Store;
import io.yodata.test.ldp.solid.server.model.storage.StoreTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assume.assumeTrue;

public class S3StoreTest extends StoreTest {

    private static AmazonS3 s3;
    private static String bucket;

    @BeforeClass
    public static void beforeClass() {
        s3 = S3Store.getClient();

        Optional<String> bucketValOpt = EnvUtils.find("TEST_S3_BUCKET_NAME");
        assumeTrue(bucketValOpt.isPresent());

        String bucketVal = bucketValOpt.get();
        assumeTrue(StringUtils.isNotBlank(bucketVal));

        bucketVal = bucketVal + "-" + UUID.randomUUID().toString().replace("-","");
        if (bucketVal.length() > 63) {
            bucketVal = bucketVal.substring(0, 63);
        }

        s3.createBucket(bucketVal);

        bucket = bucketVal;
    }

    @AfterClass
    public static void afterClass() {
        ListObjectsV2Result res;
        do {
            res = s3.listObjectsV2(bucket);
            res.getObjectSummaries().forEach(s -> s3.deleteObject(bucket, s.getKey()));
        } while (!res.getObjectSummaries().isEmpty() || !res.getCommonPrefixes().isEmpty());
        s3.deleteBucket(bucket);
    }

    @Override
    protected Store create() {
        return new S3Store(s3, bucket);
    }

}

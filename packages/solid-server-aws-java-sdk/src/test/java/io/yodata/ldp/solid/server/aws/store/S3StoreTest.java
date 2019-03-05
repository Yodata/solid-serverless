package io.yodata.ldp.solid.server.aws.store;

import com.amazonaws.SdkClientException;
import io.yodata.ldp.solid.server.model.Page;
import io.yodata.ldp.solid.server.model.Target;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class S3StoreTest {

    private static S3Store store;

    @BeforeClass
    public static void beforeClass() {
        boolean init = true;
        try {
            store = S3Store.getDefault();
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/120/a");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/130/b");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/131/c");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/132/d");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/B/2019/02/21/23/51/44/120/e");
        } catch (SdkClientException e) {
            System.err.println(e.getMessage());
            init = false;
        } finally {
            assumeTrue(init);
        }
    }

    @Test
    public void findAndListFromTsPrefix() {
        String prefix = store.getTsPrefix("1550793104131", "A/");
        assertEquals("A/2019/02/21/23/51/44/120/a", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", "1550793104129", false, true);
        assertEquals(2, p.getContains().size());
        assertEquals("b", p.getContains().get(0).getAsString());
        assertEquals("c", p.getContains().get(1).getAsString());
        assertEquals("d", p.getContains().get(2).getAsString());
    }

}

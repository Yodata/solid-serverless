package io.yodata.ldp.solid.server.aws.store;

import com.amazonaws.SdkClientException;
import io.yodata.ldp.solid.server.model.Page;
import io.yodata.ldp.solid.server.model.Target;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class S3CoreTest {

    private static S3Core store;
    private static final String byTsPodPath = "entities/localhost/data/by-ts/A";

    @BeforeClass
    public static void beforeClass() {
        boolean init = true;
        try {
            store = S3Core.getDefault();
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/120/a");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/130/b");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/131/c");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/132/d");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/21/23/51/44/140/e");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/22/01/51/44/120/f");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/A/2019/02/22/03/51/44/120/g");
            store.save("", new byte[]{}, "entities/localhost/data/by-ts/B/2019/02/22/03/51/44/120/h");
        } catch (SdkClientException e) {
            System.err.println(e.getMessage());
            init = false;
        } finally {
            assumeTrue(init);
        }
    }

    @Test
    public void findByTsBeforeFirst() {
        String ts = "0";
        String prefix = store.getTsPrefix(ts, byTsPodPath + "/");
        assertEquals(byTsPodPath + "/2019/02/21/23/51/44/120/", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", ts, false, true);
        assertEquals(p.getContains().size(), 7);
        assertEquals("a", p.getContains().get(0).getAsString());
        assertEquals("b", p.getContains().get(1).getAsString());
        assertEquals("c", p.getContains().get(2).getAsString());
        assertEquals("d", p.getContains().get(3).getAsString());
        assertEquals("e", p.getContains().get(4).getAsString());
        assertEquals("f", p.getContains().get(5).getAsString());
        assertEquals("g", p.getContains().get(6).getAsString());
    }

    @Test
    public void findByTsFirst() {
        String ts = "1550793104120";
        String prefix = store.getTsPrefix(ts, byTsPodPath + "/");
        assertEquals(byTsPodPath + "/2019/02/21/23/51/44/120/", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", ts, false, true);
        assertEquals(p.getContains().size(), 7);
        assertEquals("a", p.getContains().get(0).getAsString());
        assertEquals("b", p.getContains().get(1).getAsString());
        assertEquals("c", p.getContains().get(2).getAsString());
        assertEquals("d", p.getContains().get(3).getAsString());
        assertEquals("e", p.getContains().get(4).getAsString());
        assertEquals("f", p.getContains().get(5).getAsString());
        assertEquals("g", p.getContains().get(6).getAsString());
    }



    @Test
    public void findByTsSecond() {
        String ts = "1550793104130";
        String prefix = store.getTsPrefix(ts, byTsPodPath + "/");
        assertEquals(byTsPodPath + "/2019/02/21/23/51/44/130/", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", ts, false, true);
        assertEquals(p.getContains().size(), 6);
        assertEquals("b", p.getContains().get(0).getAsString());
        assertEquals("c", p.getContains().get(1).getAsString());
        assertEquals("d", p.getContains().get(2).getAsString());
        assertEquals("e", p.getContains().get(3).getAsString());
        assertEquals("f", p.getContains().get(4).getAsString());
        assertEquals("g", p.getContains().get(5).getAsString());
    }

    @Test
    public void findByTsNextDay() {
        String ts = "1550796704000";
        String prefix = store.getTsPrefix(ts, byTsPodPath + "/");
        assertEquals(byTsPodPath + "/2019/02/22/01/51/44/120/", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", ts, false, true);
        assertEquals(p.getContains().size(), 2);
        assertEquals("f", p.getContains().get(0).getAsString());
        assertEquals("g", p.getContains().get(1).getAsString());
    }

    @Test
    public void findByTsAfter() {
        String ts = "1577836800000";
        String prefix = store.getTsPrefix(ts, byTsPodPath + "/");
        assertEquals(byTsPodPath + "/2020/", prefix);

        Page p = store.getPage(Target.forPath(URI.create("http://localhost"), "/A/"), "timestamp", ts, false, true);
        assertEquals(p.getContains().size(), 0);
        assertNull(p.getNext());
    }

}

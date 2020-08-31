import io.yodata.ldp.solid.server.model.SolidServer;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SolidServerTest {

    @Test
    public void domainMatchingFunction() {
        assertFalse(SolidServer.DomainMatching.apply(null, null));
        assertFalse(SolidServer.DomainMatching.apply("", null));
        assertFalse(SolidServer.DomainMatching.apply(null, ""));
        assertFalse(SolidServer.DomainMatching.apply("", ""));
        assertFalse(SolidServer.DomainMatching.apply(" ", "    "));
        assertFalse(SolidServer.DomainMatching.apply("a", ""));
        assertFalse(SolidServer.DomainMatching.apply("", "b"));

        assertTrue(SolidServer.DomainMatching.apply("a", "a"));
        assertTrue(SolidServer.DomainMatching.apply("a.b", "b"));

        assertFalse(SolidServer.DomainMatching.apply("a", "b"));
        assertFalse(SolidServer.DomainMatching.apply("a.b", "c"));
        assertFalse(SolidServer.DomainMatching.apply("c", "a.b"));
    }

    @Test
    public void domainPatternMatchingFunction() {
        assertFalse(SolidServer.DomainPatternMatching.apply(null, null));
        assertFalse(SolidServer.DomainPatternMatching.apply("", null));
        assertFalse(SolidServer.DomainPatternMatching.apply(null, ""));
        assertFalse(SolidServer.DomainPatternMatching.apply("", ""));
        assertFalse(SolidServer.DomainPatternMatching.apply(" ", "    "));
        assertFalse(SolidServer.DomainPatternMatching.apply("a", ""));
        assertFalse(SolidServer.DomainPatternMatching.apply("", "b"));

        assertTrue(SolidServer.DomainPatternMatching.apply("a", "a"));
        assertTrue(SolidServer.DomainPatternMatching.apply("a.b", "*.b"));

        assertFalse(SolidServer.DomainPatternMatching.apply("a", "b"));
        assertFalse(SolidServer.DomainPatternMatching.apply("a", "a.b"));
        assertFalse(SolidServer.DomainPatternMatching.apply("a", "*.a"));
        assertFalse(SolidServer.DomainPatternMatching.apply("a.b", "c"));
        assertFalse(SolidServer.DomainPatternMatching.apply("c", "a.b"));
    }

}

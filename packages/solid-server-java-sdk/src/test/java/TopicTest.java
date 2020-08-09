import io.yodata.ldp.solid.server.model.Topic;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TopicTest {

    @Test
    public void same() {
        assertTrue(Topic.matches("test", "test"));
        assertTrue(Topic.matches("hello/world", "hello/world"));
        assertTrue(Topic.matches("prefix/main#suffix", "prefix/main#suffix"));
        assertTrue(Topic.matches(null, null));
    }

    @Test
    public void diff() {
        assertFalse(Topic.matches("test", null));
        assertFalse(Topic.matches(null, "test"));
        assertFalse(Topic.matches("hello", "world"));
    }

    @Test
    public void subset() {
        assertTrue(Topic.matches("prefix/main", "prefix/main#suffix"));
        assertTrue(Topic.matches("prefix/main", "prefix/main#"));
        assertFalse(Topic.matches("prefix/main#suffix", "prefix/main#notSuffix"));
    }

    @Test
    public void superset() {
        assertFalse(Topic.matches("prefix/main#suffix", "prefix/main"));
        assertFalse(Topic.matches("prefix/main#suffix", "prefix/main#"));
    }

}

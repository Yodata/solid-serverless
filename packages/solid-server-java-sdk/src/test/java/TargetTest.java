import io.yodata.ldp.solid.server.model.data.Target;
import org.junit.Test;

import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TargetTest {

    @Test
    public void resolve() {
        URI resolved = URI.create("https://pod.example.org/inbox/").resolve("/outbox/");
        assertEquals("https://pod.example.org/outbox/", resolved.toString());
    }

    // @Test
    public void match() {
        Target targetContainer = new Target(URI.create("/inbox/"));
        assertTrue(targetContainer.pathMatches("/inbox/"));
        assertFalse(targetContainer.pathMatches("/inbox/*"));
        assertFalse(targetContainer.pathMatches("/inbox*"));

        Target targetResource = new Target(URI.create("/inbox"));
        assertFalse(targetResource.pathMatches("/inbox/"));
        assertFalse(targetResource.pathMatches("/inbox/*"));
        assertTrue(targetResource.pathMatches("/inbox*"));
    }

}

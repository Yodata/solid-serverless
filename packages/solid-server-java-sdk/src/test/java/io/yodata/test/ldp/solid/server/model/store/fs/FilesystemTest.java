package io.yodata.test.ldp.solid.server.model.store.fs;

import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;
import io.yodata.ldp.solid.server.model.store.fs.FsPage;
import io.yodata.ldp.solid.server.model.store.fs.memory.MemoryFsElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public abstract class FilesystemTest {

    private Filesystem fs;

    protected abstract Filesystem create();

    @Before
    public void before() {
        if (Objects.isNull(fs)) {
            fs = create();
        }
    }

    private void checkMetaEquals(FsElementMeta el1, FsElementMeta el2) {
        assertEquals(el1.getContentType(), el2.getContentType());
        assertEquals(el1.getLength(), el2.getLength());
        assertEquals(el1.getProperties(), el2.getProperties());
    }

    private void checkEquals(FsElement el1, FsElement el2)  {
        checkMetaEquals(el1.getMeta(), el2.getMeta());

        try {
            byte[] el1Data = IOUtils.toByteArray(el1.getData());
            byte[] el2Data = IOUtils.toByteArray(el2.getData());
            assertEquals(el1Data, el2Data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void basicIO() {
        FsElement elementWrite = MemoryFsElement.getEmpty();
        fs.setElement("basicIO", elementWrite);
        assertTrue(fs.exists("basicIO"));
        FsElement elementRead = fs.getElement("basicIO");

        checkEquals(elementRead, elementWrite);
    }

    @Test
    public void read() {
        FsElement elBefore = MemoryFsElement.getEmpty();
        fs.setElement("read", elBefore);

        Optional<FsElementMeta> elMetaAfterFind = fs.findMeta("read");
        assertTrue(elMetaAfterFind.isPresent());
        Optional<FsElement> elAfterFind = fs.findElement("read");
        assertTrue(elAfterFind.isPresent());

        FsElementMeta elMetaAfter = fs.getMeta("read");
        FsElement elAfter = fs.getElement("read");

        checkMetaEquals(elBefore.getMeta(), elMetaAfter);
        checkMetaEquals(elMetaAfter, elMetaAfterFind.get());

        checkEquals(elBefore, elAfter);
        checkEquals(elAfter, elAfterFind.get());
    }

    @Test
    public void notFound() {
        assertFalse(fs.exists("notFound"));
        assertFalse(fs.findMeta("notFound").isPresent());
        assertFalse(fs.findElement("notFound").isPresent());
    }

    @Test(expected = NotFoundException.class)
    public void notFoundMetaException() {
        assertFalse(fs.exists("notFound"));
        fs.getMeta("notFound");
    }

    @Test(expected = NotFoundException.class)
    public void notFoundElementException() {
        assertFalse(fs.exists("notFound"));
        fs.getElement("notFound");
    }

    @Test
    public void delete() {
        FsElement element = MemoryFsElement.getEmpty();
        fs.setElement("delete", element);
        assertTrue(fs.exists("delete"));
        fs.deleteElement("delete");
        assertFalse(fs.exists("delete"));
        Optional<FsElementMeta> metaReadFind = fs.findMeta("delete");
        assertFalse(metaReadFind.isPresent());
        Optional<FsElement> elReadFind = fs.findElement("delete");
        assertFalse(elReadFind.isPresent());
    }

    @Test
    public void list() {
        FsElement el1 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 1));
        FsElement el2 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 2));
        FsElement el3 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 3));
        FsElement el4 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 4));
        FsElement el5 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 5));
        FsElement el6 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 6));
        FsElement el7 = MemoryFsElement.forJson(GsonUtil.makeObj("body", 7));

        fs.setElement("list/a/1", el1);
        fs.setElement("list/b/2", el2);
        fs.setElement("list/b/3", el3);
        fs.setElement("list/b/4", el4);
        fs.setElement("list/b/5", el5);
        fs.setElement("list/c/6", el6);

        FsPage pageA = fs.listElements("list/a/", "", 2);
        assertEquals(1, pageA.getElements().size());
        assertEquals("1", pageA.getElements().get(0));

        FsPage pageB1 = fs.listElements("list/b/", "", 2);
        assertNotEquals(pageA.getNext(), pageB1.getNext());
        assertEquals(2, pageB1.getElements().size());
        assertEquals("2", pageB1.getElements().get(0));
        assertEquals("3", pageB1.getElements().get(1));

        FsPage pageB2 = fs.listElements("list/b/", pageB1.getNext(), 2);
        assertNotEquals(pageB1.getNext(), pageB2.getNext());
        assertEquals(2, pageB2.getElements().size());
        assertEquals("4", pageB2.getElements().get(0));
        assertEquals("5", pageB2.getElements().get(1));

        FsPage pageB3 = fs.listElements("list/b/", pageB2.getNext(), 2);
        assertEquals(pageB2.getNext(), pageB3.getNext());
        assertEquals(0, pageB3.getElements().size());

        FsPage pageC1 = fs.listElements("list/c/", "", 2);
        assertTrue(StringUtils.isNotEmpty(pageC1.getNext()));
        assertEquals(1, pageC1.getElements().size());
        assertEquals("6", pageC1.getElements().get(0));

        FsPage pageC2 = fs.listElements("list/c/", pageC1.getNext(), 2);
        assertEquals(pageC1.getNext(), pageC2.getNext());
        assertEquals(0, pageC2.getElements().size());

        fs.setElement("list/c/7", el7);

        FsPage pageC3 = fs.listElements("list/c/", pageC2.getNext(), 2);
        assertTrue(StringUtils.isNotEmpty(pageC3.getNext()));
        assertEquals(1, pageC3.getElements().size());
        assertEquals("7", pageC3.getElements().get(0));
    }

}

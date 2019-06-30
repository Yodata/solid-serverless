package io.yodata.test.ldp.solid.server.model.storage;

import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.storage.Store;
import io.yodata.ldp.solid.server.model.storage.StoreElement;
import io.yodata.ldp.solid.server.model.storage.StoreElementMeta;
import io.yodata.ldp.solid.server.model.storage.StoreElementPage;
import io.yodata.ldp.solid.server.model.storage.memory.MemoryStore;
import io.yodata.ldp.solid.server.model.storage.memory.MemoryStoreElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public abstract class StoreTest {

    private Store store;

    protected abstract Store create();

    @Before
    public void before() {
        if (Objects.isNull(store)) {
            store = create();
        }
    }

    private void checkMetaEquals(StoreElementMeta el1, StoreElementMeta el2) {
        assertEquals(el1.getContentType(), el2.getContentType());
        assertEquals(el1.getLength(), el2.getLength());
        assertEquals(el1.getProperties(), el2.getProperties());
    }

    private void checkEquals(StoreElement el1, StoreElement el2)  {
        checkMetaEquals(el1, el2);

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
        StoreElement elementWrite = MemoryStoreElement.getEmpty();
        store.setElement("basicIO", elementWrite);
        assertTrue(store.exists("basicIO"));
        StoreElement elementRead = store.getElement("basicIO");

        checkEquals(elementRead, elementWrite);
    }

    @Test
    public void read() {
        StoreElement elBefore = MemoryStoreElement.getEmpty();
        store.setElement("read", elBefore);

        Optional<StoreElementMeta> elMetaAfterFind = store.findMeta("read");
        assertTrue(elMetaAfterFind.isPresent());
        Optional<StoreElement> elAfterFind = store.findElement("read");
        assertTrue(elAfterFind.isPresent());

        StoreElementMeta elMetaAfter = store.getMeta("read");
        StoreElement elAfter = store.getElement("read");

        checkMetaEquals(elBefore, elMetaAfter);
        checkMetaEquals(elMetaAfter, elMetaAfterFind.get());

        checkEquals(elBefore, elAfter);
        checkEquals(elAfter, elAfterFind.get());
    }

    @Test
    public void notFound() {
        assertFalse(store.exists("notFound"));
        assertFalse(store.findMeta("notFound").isPresent());
        assertFalse(store.findElement("notFound").isPresent());
    }

    @Test(expected = NotFoundException.class)
    public void notFoundMetaException() {
        assertFalse(store.exists("notFound"));
        store.getMeta("notFound");
    }

    @Test(expected = NotFoundException.class)
    public void notFoundElementException() {
        assertFalse(store.exists("notFound"));
        store.getElement("notFound");
    }

    @Test
    public void delete() {
        StoreElement element = MemoryStoreElement.getEmpty();
        store.setElement("delete", element);
        assertTrue(store.exists("delete"));
        store.deleteElement("delete");
        assertFalse(store.exists("delete"));
        Optional<StoreElementMeta> metaReadFind = store.findMeta("delete");
        assertFalse(metaReadFind.isPresent());
        Optional<StoreElement> elReadFind = store.findElement("delete");
        assertFalse(elReadFind.isPresent());
    }

    @Test
    public void list() {
        StoreElement el1 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 1));
        StoreElement el2 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 2));
        StoreElement el3 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 3));
        StoreElement el4 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 4));
        StoreElement el5 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 5));
        StoreElement el6 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 6));
        StoreElement el7 = MemoryStoreElement.forJson(GsonUtil.makeObj("body", 7));

        store.setElement("list/a/1", el1);
        store.setElement("list/b/2", el2);
        store.setElement("list/b/3", el3);
        store.setElement("list/b/4", el4);
        store.setElement("list/b/5", el5);
        store.setElement("list/c/6", el6);

        StoreElementPage page = store.listElements("list/", "", Integer.MAX_VALUE);
        assertEquals(3, page.getElements().size());
        assertEquals("a/", page.getElements().get(0));
        assertEquals("b/", page.getElements().get(1));
        assertEquals("c/", page.getElements().get(2));

        StoreElementPage pageA = store.listElements("list/a/", "", 2);
        assertEquals(1, pageA.getElements().size());
        assertEquals("1", pageA.getElements().get(0));

        StoreElementPage pageB1 = store.listElements("list/b/", "", 2);
        assertNotEquals(pageA.getNext(), pageB1.getNext());
        assertEquals(2, pageB1.getElements().size());
        assertEquals("2", pageB1.getElements().get(0));
        assertEquals("3", pageB1.getElements().get(1));

        StoreElementPage pageB2 = store.listElements("list/b/", pageB1.getNext(), 2);
        assertNotEquals(pageB1.getNext(), pageB2.getNext());
        assertEquals(2, pageB2.getElements().size());
        assertEquals("4", pageB2.getElements().get(0));
        assertEquals("5", pageB2.getElements().get(1));

        StoreElementPage pageB3 = store.listElements("list/b/", pageB2.getNext(), 2);
        assertEquals(pageB2.getNext(), pageB3.getNext());
        assertEquals(0, pageB3.getElements().size());

        StoreElementPage pageC1 = store.listElements("list/c/", "", 2);
        assertTrue(StringUtils.isNotEmpty(pageC1.getNext()));
        assertEquals(1, pageC1.getElements().size());
        assertEquals("6", pageC1.getElements().get(0));

        StoreElementPage pageC2 = store.listElements("list/c/", pageC1.getNext(), 2);
        assertEquals(pageC1.getNext(), pageC2.getNext());
        assertEquals(0, pageC2.getElements().size());

        store.setElement("list/c/7", el7);

        StoreElementPage pageC3 = store.listElements("list/c/", pageC2.getNext(), 2);
        assertTrue(StringUtils.isNotEmpty(pageC3.getNext()));
        assertEquals(1, pageC3.getElements().size());
        assertEquals("7", pageC3.getElements().get(0));
    }

}

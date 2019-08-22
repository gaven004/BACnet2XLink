package com.serotonin.bacnet4j.obj;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.obj.fileAccess.CrlfDelimitedFileAccess;
import com.serotonin.bacnet4j.obj.fileAccess.StreamAccess;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class FileObjectTest extends AbstractTest {
    private final String filename = "fileObjectTest.txt";
    private final String path = getClass().getClassLoader().getResource(filename).getPath();

    @Test
    public void streamReadFileSize() throws Exception {
        final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(new File(path)));
        assertEquals(new UnsignedInteger(922), f.readProperty(PropertyIdentifier.fileSize, null));
    }

    @Test
    public void recordReadFileSize() throws Exception {
        final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(new File(path)));
        assertEquals(new UnsignedInteger(922), f.readProperty(PropertyIdentifier.fileSize, null));
    }

    @Test
    public void streamWriteFileSize() throws Exception {
        // Write a zero file size.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, UnsignedInteger.ZERO);
            assertEquals(UnsignedInteger.ZERO, f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write > 0 and < file size.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(100));
            assertEquals(new UnsignedInteger(100), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a file size == size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(922));
            assertEquals(new UnsignedInteger(922), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a file size > size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(1001));
            assertEquals(new UnsignedInteger(1001), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });
    }

    @Test
    public void recordWriteFileSize() throws Exception {
        // Write a zero record count.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, UnsignedInteger.ZERO);
            assertEquals(UnsignedInteger.ZERO, f.readProperty(PropertyIdentifier.recordCount, null));
            assertEquals(UnsignedInteger.ZERO, f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write > 0 and < size record count.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(100));
            assertEquals(new UnsignedInteger(2), f.readProperty(PropertyIdentifier.recordCount, null));
            assertEquals(new UnsignedInteger(100), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write > 0 and < size record count.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            //Since Newlines are CRLF on windows and CR on OSX and LF on Unix
            if(SystemUtils.IS_OS_WINDOWS)
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(921));
            else
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(907));
            assertEquals(new UnsignedInteger(14), f.readProperty(PropertyIdentifier.recordCount, null));
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(921), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(907), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a record count == size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            if(SystemUtils.IS_OS_WINDOWS)
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(922));
            else
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(908));
            assertEquals(new UnsignedInteger(14), f.readProperty(PropertyIdentifier.recordCount, null));
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(922), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(908), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a record count > size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            if(SystemUtils.IS_OS_WINDOWS)
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(1001));
            else
                f.writeProperty(null, PropertyIdentifier.fileSize, new UnsignedInteger(948));
            
            assertEquals(new UnsignedInteger(54), f.readProperty(PropertyIdentifier.recordCount, null));
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(1002), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(948), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });
    }

    @Test
    public void readOnly() throws Exception {
        final File file = new File(path);
        final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(file));

        
        if (file.setWritable(true)) {
            assertEquals(Boolean.FALSE, f.readProperty(PropertyIdentifier.readOnly, null));
        }

        if (file.setWritable(false)) {
            assertEquals(Boolean.TRUE, f.readProperty(PropertyIdentifier.readOnly, null));
            file.setWritable(true);
        }
    }

    @Test
    public void streamReadRecordCount() throws Exception {
        final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(new File(path)));
        TestUtils.assertBACnetServiceException(() -> {
            f.readProperty(PropertyIdentifier.recordCount, null);
        }, ErrorClass.property, ErrorCode.readAccessDenied);
    }

    @Test
    public void recordReadRecordCount() throws Exception {
        final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(new File(path)));
        assertEquals(new UnsignedInteger(14), f.readProperty(PropertyIdentifier.recordCount, null));
    }

    @Test
    public void streamWriteRecordCount() throws Exception {
        final FileObject f = new FileObject(d1, 0, "test", new StreamAccess(new File(path)));
        TestUtils.assertBACnetServiceException(() -> {
            f.writeProperty(null, new PropertyValue(PropertyIdentifier.recordCount, UnsignedInteger.ZERO));
        }, ErrorClass.property, ErrorCode.writeAccessDenied);
    }

    @Test
    public void recordWriteRecordCount() throws Exception {
        // Write a zero record count.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.recordCount, UnsignedInteger.ZERO);
            assertEquals(UnsignedInteger.ZERO, f.readProperty(PropertyIdentifier.recordCount, null));
            assertEquals(UnsignedInteger.ZERO, f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write > 0 and < size record count.
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.recordCount, new UnsignedInteger(10));
            assertEquals(new UnsignedInteger(10), f.readProperty(PropertyIdentifier.recordCount, null));
            //Since Newlines are CRLF on windows and CR on OSX and LF on Unix
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(665), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(655), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a record count == size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.recordCount, new UnsignedInteger(14));
            assertEquals(new UnsignedInteger(14), f.readProperty(PropertyIdentifier.recordCount, null));
            //Since Newlines are CRLF on windows and CR on OSX and LF on Unix
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(922), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(908), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });

        // Write a record count > size
        doInCopy((file) -> {
            final FileObject f = new FileObject(d1, 0, "test", new CrlfDelimitedFileAccess(file));
            f.writeProperty(null, PropertyIdentifier.recordCount, new UnsignedInteger(25));
            assertEquals(new UnsignedInteger(25), f.readProperty(PropertyIdentifier.recordCount, null));
            //Since Newlines are CRLF on windows and CR on OSX and LF on Unix
            if(SystemUtils.IS_OS_WINDOWS)
                assertEquals(new UnsignedInteger(944), f.readProperty(PropertyIdentifier.fileSize, null));
            else
                assertEquals(new UnsignedInteger(919), f.readProperty(PropertyIdentifier.fileSize, null));
            d1.removeObject(f.getId());
        });
    }

    @FunctionalInterface
    static interface InCopyCommand {
        void doInCopy(File file) throws Exception;
    }

    private void doInCopy(final InCopyCommand command) throws Exception {
        final File source = new File(getClass().getClassLoader().getResource(filename).toURI());
        final File target = new File(source.getParentFile(), source.getName() + ".tmp");
        if (target.exists())
            target.delete();
        Files.copy(source.toPath(), target.toPath());
        command.doInCopy(target);
        target.delete();
    }
}

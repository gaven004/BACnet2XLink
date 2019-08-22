package com.serotonin.bacnet4j.type.constructed;

import org.junit.Test;

import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.OctetString;
import com.serotonin.bacnet4j.type.primitive.Time;

public class NameValueTest {
    @Test
    public void characterString() {
        final NameValue nv = new NameValue("tagName", new CharacterString("tagValue"));
        TestUtils.assertEncoding(nv, "0d08007461674e616d6575090074616756616c7565");
    }

    @Test
    public void octetString() {
        final NameValue nv = new NameValue("tagName", new OctetString(new byte[] { 0, 1, 2, 3, 4, 5 }));
        TestUtils.assertEncoding(nv, "0d08007461674e616d656506000102030405");
    }

    @Test
    public void dateTime() {
        final NameValue nv = new NameValue("tagName", DateTime.UNSPECIFIED);
        TestUtils.assertEncoding(nv, "0d08007461674e616d65a4ffffffffb4ffffffff");
    }

    @Test
    public void optional() {
        final NameValue nv = new NameValue("tagName");
        TestUtils.assertEncoding(nv, "0d08007461674e616d65");
    }

    @Test
    public void sequence() {
        final SequenceOf<NameValue> seq = new SequenceOf<>( //
                new NameValue("t1", CharacterString.EMPTY), //
                new NameValue("t2"), //
                new NameValue("t3", new CharacterString("v1")), //
                new NameValue("t4", DateTime.UNSPECIFIED), //
                new NameValue("t6", Date.UNSPECIFIED), //
                new NameValue("t7", Time.UNSPECIFIED), //
                new NameValue("t5", Null.instance));
        TestUtils.assertSequenceEncoding(seq, NameValue.class,
                "0b00743171000b0074320b007433730076310b007434a4ffffffffb4ffffffff0b007436a4ffffffff0b007437b4ffffffff0b00743500");
    }
}

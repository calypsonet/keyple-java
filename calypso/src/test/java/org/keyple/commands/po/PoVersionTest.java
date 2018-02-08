package org.keyple.commands.po;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.keyple.transaction.PoSecureSession;
import org.keyple.commands.po.PoRevision;

//@RunWith(MockitoJUnitRunner.class)
public class PoVersionTest {

    // @InjectMocks
    // ResponseUtils responseUtils;

    // @Mock
    @Test
    public void computePoRevision() {

        // 01h to 04h Calypso Rev 1 or Rev 2 (depending on Application Subtype)
        // 06h to 1Fh Calypso Rev 2
        // 20h to 7Fh Calypso Rev.3
        assertEquals(PoSecureSession.computePoRevision((byte) 0x01), PoRevision.REV2_4);
        assertEquals(PoSecureSession.computePoRevision((byte) 0x04), PoRevision.REV2_4);
        assertEquals(PoSecureSession.computePoRevision((byte) 0x06), PoRevision.REV2_4);
        assertEquals(PoSecureSession.computePoRevision((byte) 0x1F), PoRevision.REV2_4);
        assertEquals(PoSecureSession.computePoRevision((byte) 0x20), PoRevision.REV3_1);
        assertEquals(PoSecureSession.computePoRevision((byte) 0x7F), PoRevision.REV3_2);

    }

}

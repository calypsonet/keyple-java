/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.S1D;
import static org.junit.Assert.*;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.AnswerToReset;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SelectionStatus;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Test;

public class CalypsoSamTest {
    /** basic CalypsoSam test: nominal ATR parsing */
    @Test
    public void test_CalypsoSam_1() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        CalypsoSam calypsoSam = new CalypsoSam(samSelectionRequest);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180D202030411223344829000")),
                null, true);
        calypsoSam.setSelectionResponse(new SeResponse(true, selectionStatus, null));
        assertEquals(S1D, calypsoSam.getSamRevision());
        assertEquals((byte) 0x80, calypsoSam.getApplicationType());
        assertEquals((byte) 0xD2, calypsoSam.getApplicationSubType());
        assertEquals((byte) 0x01, calypsoSam.getPlatform());
        assertEquals((byte) 0x02, calypsoSam.getSoftwareIssuer());
        assertEquals((byte) 0x03, calypsoSam.getSoftwareVersion());
        assertEquals((byte) 0x04, calypsoSam.getSoftwareRevision());
        assertEquals("11223344", ByteArrayUtils.toHex(calypsoSam.getSerialNumber()));
    }
}

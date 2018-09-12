/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.csm.parser;

import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.InconsistentParameterValueException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DigestAuthenticateRespParsTest {

    @Test
    public void digestAuthenticateResp() throws InconsistentParameterValueException {

        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(ByteBuffer.wrap(new byte[] {90, 0}), null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteBufferUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser = new DigestAuthenticateRespPars(
                seResponse.getSingleResponse().getApduResponses().get(0));
        ByteBuffer responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertEquals(ByteBuffer.wrap(new byte[] {90, 0}), responseActual);
    }
}
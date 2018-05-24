/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Simulated Calypso PO embedded in a ISO Secured Element
 * You can add additional behaviour at runtime using
 * {@link #addCommand(String request, String response)} and {@link #removeCommand(String)}
 * 
 */

public class StubHoplinkPO extends StubSecureElement {

    private static final ILogger logger = SLoggerFactory.getLogger(StubHoplinkPO.class);


    String tech = "android.nfc.tech.IsoDep";

    private Map<String, String> commands;


    public StubHoplinkPO() {

        commands = new HashMap<String, String>();
        commands.put("00B201A420",
                "00000000000000000000000000000000000000000000000000000000000000009000");
        commands.put("00B201D430",
                "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000");
        commands.put(
                "00DC01D4300102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F30",
                "9000");

    }

    @Override
    public String getTech() {
        return tech;
    }

    @Override
    public String getAid() {
        return "A000000291A000000191";
    }

    @Override
    public String getFCI() {
        return "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000";
    }

    @Override
    public String getATR() {
        return null;
    }

    @Override
    public ApduResponse process(ApduRequest request) {

        String out = null;
        Boolean success;
        String commandHex = ByteBufferUtils.toHex(request.getBuffer());
        logger.info("Processing command : " + commandHex);

        if (commands.containsKey(commandHex)) {
            out = commands.get(commandHex);
            success = true;
        } else {
            success = false;
        }
        logger.info("Result command : " + out);

        return new ApduResponse(ByteBufferUtils.fromHex(out), success);

    }

    public void addCommand(String request, String response) {
        commands.put(request, response);
    }

    public void removeCommand(String request) {
        commands.remove(request);
    }


}

/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticReader;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;
import android.util.Log;

/**
 * Communicates with Android readers throught the Open Mobile API see {@link Reader} Instances of
 * this class represent SE readers supported by this device. These readers can be physical devices
 * or virtual devices. They can be removable or not. They can contain one SE that can or cannot be
 * removed.
 */
public final class AndroidOmapiReader extends AbstractStaticReader {


    private static final String TAG = AndroidOmapiReader.class.getSimpleName();

    private Reader omapiReader;
    private Session session = null;
    private Channel openChannel = null;
    private byte[] openApplication = null;
    private Map<String, String> parameters = new HashMap<String, String>();

    protected AndroidOmapiReader(String pluginName, Reader omapiReader, String readerName) {
        super(pluginName, readerName);
        this.omapiReader = omapiReader;
    }


    @Override
    public Map<String, String> getParameters() {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReader");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReader");
        parameters.put(key, value);
    }

    /**
     * The transmission mode is always CONTACTS in an OMAPI reader
     *
     * @return the current transmission mode
     */
    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACTS;
    }

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
     * 
     * @return True if the SE is present, false otherwise
     * @throws KeypleReaderException
     */
    @Override
    protected boolean checkSePresence() throws NoStackTraceThrowable {
        return omapiReader.isSecureElementPresent();
    }

    /**
     * Get the SE Answer To Reset
     * @return a byte array containing the ATR or null if no session was available
     */
    @Override
    protected byte[] getATR() {
        if(session != null) {
            Log.i(TAG, "Retrieveing ATR from session...");
            return session.getATR();
        }
        else {
            return null;
        }
    }

    /**
     * Operate a ATR based logical channel opening.
     * <p>
     * An OMAPI basic channel is open and the ATR is checked with the regular expression available
     * in the AtrSelector.
     * @param atrSelector the ATR matching data (a regular expression used to compare the SE ATR to
     *        the expected one)
     * @return the SelectionStatus
     * @throws KeypleIOReaderException if an IOException occurs
     */
    @Override
    protected final SelectionStatus openLogicalChannelByAtr(SeRequest.AtrSelector atrSelector)
            throws KeypleIOReaderException {
        boolean selectionHasMatched;
        byte[] atr = getATR();

        if (atr == null) {
            throw new KeypleIOReaderException("Didn't get an ATR from the SE.");
        }

        if (session == null) {
            throw new KeypleIOReaderException("Session is null.");
        }

        try {
            openChannel = session.openBasicChannel(null);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeypleIOReaderException("IOException while opening basic channel.");
        }

        if (openChannel == null) {
            throw new KeypleIOReaderException("Failed to open a basic channel.");
        }

        Log.d(TAG,"[" + this.getName()+"] openLogicalChannelByAtr => ATR: " + ByteArrayUtils.toHex(atr));

        if (atrSelector.atrMatches(atr)) {
            selectionHasMatched = true;
        } else {
            Log.d(TAG,"[" + this.getName() + "] openLogicalChannelByAtr => ATR Selection failed. SELECTOR = " + atrSelector);
            selectionHasMatched = false;
        }
        return new SelectionStatus(new AnswerToReset(atr), new ApduResponse(null, null),
                selectionHasMatched);
    }

    /**
     * Operate a AID based logical channel opening.
     * <p>
     * An OMAPI logical channel is open and the select response is checked with successful codes available
     * in the AidSelector.
     * @param aidSelector the targeted application selector
     * @return the SelectionStatus
     * @throws KeypleIOReaderException if an IOException occurs
     */
    @Override
    protected final SelectionStatus openLogicalChannelByAid(SeRequest.AidSelector aidSelector) throws KeypleIOReaderException, KeypleApplicationSelectionException {
        ApduResponse fciResponse;
        byte[] atr;

        byte[] aid = aidSelector.getAidToSelect();

        if (aid != null) {
            Log.i(TAG, "Create logical openChannel within the session...");
            try {
                openChannel = session.openLogicalChannel(aid);
            } catch (IOException e) {
                e.printStackTrace();
                throw new KeypleIOReaderException("IOException while opening logical channel.");
            } catch (NoSuchElementException e) {
                throw new KeypleApplicationSelectionException(
                        "Error while selecting application : " + ByteArrayUtils.toHex(aid), e);
            }

            if (openChannel == null) {
                throw new KeypleIOReaderException("Failed to open a logical channel.");
            }

            /* get the FCI and build an ApduResponse */
            fciResponse = new ApduResponse(openChannel.getSelectResponse(), aidSelector.getSuccessfulSelectionStatusCodes());

            /* get the ATR*/
            atr = getATR();
        } else {
            throw new IllegalArgumentException("AID must not be null for an AidSelector.");
        }
        return new SelectionStatus(new AnswerToReset(atr), fciResponse,
                fciResponse.isSuccessful());
    }

    @Override
    public boolean isPhysicalChannelOpen() {
        if(session != null && !session.isClosed()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openPhysicalChannel() throws KeypleChannelStateException {
        try {
            session = omapiReader.openSession();
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeypleChannelStateException("IOException while opening physical channel.");
        }
    }

//    /**
//     * Open a Channel to the application AID if not open yet. see {@link Reader#openSession()} see
//     * {@link Session#openLogicalChannel(byte[])}
//     *
//     * @param selector: AID of the application to select
//     * @return Array : index[0] : ATR and index[1] :FCI
//     * @throws KeypleReaderException
//     */
//    @Override
//    protected SelectionStatus openLogicalChannelAndSelect(SeRequest.Selector selector,
//            Set<Integer> successfulSelectionStatusCodes)
//            throws KeypleChannelStateException, KeypleApplicationSelectionException {
//        byte[][] atrAndFci = new byte[2][];
//        byte[] aid = ((SeRequest.AidSelector) selector).getAidToSelect();
//        try {
//            if (openChannel != null && !openChannel.isClosed() && openApplication != null
//                    && openApplication.equals(aid)) {
//                Log.i(TAG, "Channel is already open to aid : " + ByteArrayUtils.toHex(aid));
//
//                atrAndFci[0] = openChannel.getSession().getATR();
//                atrAndFci[1] = openChannel.getSelectResponse();
//
//
//            } else {
//
//                Log.i(TAG, "Opening channel to aid : " + ByteArrayUtils.toHex(aid));
//
//                // open physical channel
//                Session session = omapiReader.openSession();
//
//                // get ATR from session
//                Log.i(TAG, "Retrieveing ATR from session...");
//                atrAndFci[0] = session.getATR();
//
//                Log.i(TAG, "Create logical openChannel within the session...");
//                openChannel = session.openLogicalChannel(aid);
//
//                // get FCI
//                atrAndFci[1] = openChannel.getSelectResponse();
//
//            }
//        } catch (IOException e) {
//            throw new KeypleChannelStateException(
//                    "Error while opening channel, aid :" + ByteArrayUtils.toHex(aid), e.getCause());
//        } catch (SecurityException e) {
//            throw new KeypleChannelStateException(
//                    "Error while opening channel, aid :" + ByteArrayUtils.toHex(aid), e.getCause());
//        } catch (NoSuchElementException e) {
//            throw new KeypleApplicationSelectionException(
//                    "Error while selecting application : " + ByteArrayUtils.toHex(aid), e);
//        }
//
//        return new SelectionStatus(new AnswerToReset(atrAndFci[0]),
//                new ApduResponse(atrAndFci[1], null), true);
//    }

    /**
     * Close session see {@link Session#close()}
     * 
     * @throws KeypleReaderException
     */
    @Override
    protected void closePhysicalChannel() {
        // close physical channel if exists
        if (openApplication != null) {
            openChannel.getSession().close();
            openChannel = null;
            openApplication = null;
        }
    }

    /**
     * Transmit an APDU command (as per ISO/IEC 7816) to the SE see {@link Channel#transmit(byte[])}
     * 
     * @param apduIn byte buffer containing the ingoing data
     * @return
     * @throws KeypleReaderException
     */
    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.length);
        Log.d(TAG, "Data in : " + ByteArrayUtils.toHex(apduIn));
        byte[] data = apduIn;
        byte[] dataOut = new byte[0];
        try {
            dataOut = openChannel.transmit(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeypleIOReaderException("Error while transmitting APDU", e);
        }
        byte[] out = dataOut;
        Log.d(TAG, "Data out : " + ByteArrayUtils.toHex(out));
        return out;
    }

    /**
     * The only protocol Fla
     * 
     * @param protocolFlag
     * @return true
     * @throws KeypleReaderException
     */
    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return protocolFlag.equals(ContactsProtocols.PROTOCOL_ISO7816_3);
    }
}

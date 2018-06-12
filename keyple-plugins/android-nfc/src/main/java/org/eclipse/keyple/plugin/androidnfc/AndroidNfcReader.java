/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;


/**
 * Implementation of {@link org.eclipse.keyple.seproxy.ProxyReader} for the communication with the
 * NFC Tag though Android {@link NfcAdapter}
 */
public class AndroidNfcReader extends AbstractLocalReader implements NfcAdapter.ReaderCallback {

    private static final String TAG = "AndroidNfcReader";

    // keep state between session if required
    private TagTransceiver tagTransceiver;

    /**
     * Private constructor
     */
    private AndroidNfcReader() {
        Log.i(TAG, "Instanciate singleton NFC Reader");
    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        /**
         * Unique instance no-preinitialized
         */
        private final static AndroidNfcReader instance = new AndroidNfcReader();
    }


    /**
     * Access point for the unique instance of singleton
     */
    protected static AndroidNfcReader getInstance() {
        return SingletonHolder.instance;
    }


    @Override
    public String getName() {
        return "AndroidNfcReader";
    }

    @Override
    public Map<String, String> getParameters() {
        return new HashMap<String, String>();
    }

    @Override
    public void setParameter(String key, String value) throws IOException {
        Log.w(TAG, "AndroidNfcReader does not support parameters");
    }


    /**
     * Callback function invoked when the @{@link NfcAdapter} detects a @{@link Tag}
     * 
     * @param tag : detected tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i(TAG, "Received Tag Discovered event ");
        try {
            tagTransceiver = TagTransceiver.getTagTransceiver(tag);
            notifyObservers(ReaderEvent.SE_INSERTED);

        } catch (IOReaderException e) {
            e.printStackTrace();
            // TODO
        }

    }


    @Override
    public boolean isSePresent() {

        return tagTransceiver != null && tagTransceiver.isConnected();
    }

    @Override
    public void checkOrOpenPhysicalChannel() throws IOReaderException {

        if (!isSePresent()) {
            try {
                tagTransceiver.connect();
                Log.i(TAG, "Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to Tag ");
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Tag is already connected to : " + printTagId());
        }
    }

    @Override
    public void closePhysicalChannel() throws IOReaderException {
        try {
            if (tagTransceiver != null) {
                tagTransceiver.close();
                this.notifyObservers(ReaderEvent.SE_REMOVAL);
                Log.i(TAG, "Disconnected tag : " + printTagId());
            }

        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
        }

        tagTransceiver = null;
    }


    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        long commandLenght = apduIn.limit();
        Log.d(TAG, "Data Length to be sent to tag : " + commandLenght);
        Log.d(TAG, "Data in : " + ByteBufferUtils.toHex(apduIn));
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        byte[] dataOut = new byte[0];
        try {
            dataOut = tagTransceiver.transceive(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        ByteBuffer out = ByteBuffer.wrap(dataOut);
        Log.d(TAG, "Data out : " + ByteBufferUtils.toHex(out));
        return out;
    }

    @Override
    public ByteBuffer getAlternateFci() {
        return null;// TODO
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {

        return protocolFlag == ContactlessProtocols.PROTOCOL_ISO14443_4
                && tagTransceiver.getTech().equals(AndroidNfcProtocolSettings.TAG_TECHNOLOGY_ISO14443_4)
                || protocolFlag == ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC && tagTransceiver
                .getTech().equals(AndroidNfcProtocolSettings.TAG_TECHNOLOGY_MIFARE_CLASSIC)
                || protocolFlag == ContactlessProtocols.PROTOCOL_MIFARE_UL && tagTransceiver
                .getTech().equals(AndroidNfcProtocolSettings.TAG_TECHNOLOGY_MIFARE_UL);
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    protected void processIntent(Intent intent) {

        // Extract Tag from Intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }

    private String printTagId() {
        return tagTransceiver != null && tagTransceiver.getTag() != null
                ? tagTransceiver.getTag().getId() + tagTransceiver.getTag().toString()
                : "null";
    }
}

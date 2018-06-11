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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
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
 * Implementation of @{@link org.eclipse.keyple.seproxy.ProxyReader} for the communication with the
 * ISO Card though Android @{@link NfcAdapter}
 */
public class AndroidNfcReader extends AbstractLocalReader
        implements NfcAdapter.ReaderCallback {

    private static final String TAG = "AndroidNfcReader";

    // keep state between session if required
    private TagTransceiver tagTransceiver;
    private ByteBuffer previousOpenApplication = null;


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
        Log.w(TAG,"AndroidNfcReader does not support parameters");
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
            //TODO
        }

    }


    @Override
    public boolean isSePresent() {

        return tagTransceiver != null && tagTransceiver.isConnected();
    }

    @Override
    public void checkOrOpenPhysicalChannel() throws IOReaderException {

        if(!isSePresent()){
            try {
                tagTransceiver.connect();
                Log.i(TAG, "Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to Tag ");
                e.printStackTrace();
            }
        }else{
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
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        Log.d(TAG, "Data in : " + data);
        byte[] dataOut = new byte[0];
        try {
            dataOut = tagTransceiver.transceive(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        Log.d(TAG, "Data out  : " + dataOut);
        return ByteBuffer.wrap(dataOut);
    }

    @Override
    public ByteBuffer getAlternateFci() {
        return null;//TODO
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {

            return protocolFlag == ContactlessProtocols.PROTOCOL_ISO14443_4 &&
                    tagTransceiver.getTech() == AndroidNfcProtocolSettings.TAG_TECHNOLOGY_ISO14443_4
                    ||
                    protocolFlag == ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC &&
                    tagTransceiver.getTech() == AndroidNfcProtocolSettings.TAG_TECHNOLOGY_MIFARE_CLASSIC
                    ||
                    protocolFlag == ContactlessProtocols.PROTOCOL_MIFARE_UL &&
                    tagTransceiver.getTech() == AndroidNfcProtocolSettings.TAG_TECHNOLOGY_MIFARE_UL;
    }

//    /**
//     * Transmit {@link SeRequestSet} to the connected Tag Supports protocol argument to
//     * filterByProtocol commands for the right connected Tag
//     *
//     * @param seRequest the se application request
//     * @return {@link SeResponseSet} : response from the transmitted request
//     */
//    @Override
//    public SeResponseSet transmit(SeRequestSet seRequest) {
//        Log.i(TAG, "Calling transmit on Android NFC Reader");
//        Log.d(TAG, "Size of APDU Requests : " + String.valueOf(seRequest.getRequests().size()));
//
//        // init response
//        List<SeResponse> seResponseElements = new ArrayList<SeResponse>();
//
//        // Filter requestElements whom protocol matches the current tag
//        List<SeRequest> seRequestElements = filterByProtocol(seRequest.getRequests());
//
//        // no seRequestElements are left after filtering
//        if (seRequestElements.size() < 1) {
//            disconnectTag();
//            return new SeResponseSet(seResponseElements);
//
//        }
//
//
//        // process the request elements
//        for (int i = 0; i < seRequestElements.size(); i++) {
//
//            SeRequest seRequestElement = seRequestElements.get(i);
//
//            // init response
//            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
//            ApduResponse fciResponse = null;
//
//            try {
//
//                // Checking of the presence of the AID request in requests group
//                ByteBuffer aid = seRequestElement.getAidToSelect();
//
//                // Open the application channel if not open yet
//                if (previousOpenApplication == null || previousOpenApplication != aid) {
//                    Log.i(TAG, "Connecting to application : " + aid);
//                    fciResponse = this.connectApplication(seRequestElement.getAidToSelect());
//                }
//
//                // Send all apduRequest
//                for (ApduRequest apduRequest : seRequestElement.getApduRequests()) {
//                    apduResponses.add(sendAPDUCommand(apduRequest.getBuffer()));
//                }
//
//                // Add ResponseElements to global SeResponseSet
//                SeResponse out =
//                        new SeResponse(previousOpenApplication != null, fciResponse, apduResponses);
//                seResponseElements.add(out);
//
//                // Don't process more seRequestElement if asked
//                if (seRequestElement.isKeepChannelOpen()) {
//                    Log.i(TAG,
//                            "Keep Channel Open is set to true, abort further seRequestElement if any");
//                    saveChannelState(aid);
//                    break;
//                }
//
//                // For last element, close physical channel if asked
//                if (i == seRequestElements.size() - 1 && !seRequestElement.isKeepChannelOpen()) {
//                    disconnectTag();
//                }
//
//            } catch (IOException e) {
//                Log.e(TAG, "Error executing command");
//                e.printStackTrace();
//                apduResponses.add(null);// add empty response
//            }
//
//        }
//
//        return new SeResponseSet(seResponseElements);
//    }


//    /**
//     * Filter seRequestElements based on their protocol and the tag detected
//     *
//     * @param seRequestElements embedding seRequestElements to be filtered
//     * @return filtered seRequest
//     */
//    private List<SeRequest> filterByProtocol(List<SeRequest> seRequestElements) {
//
//
//        Log.d(TAG, "Filtering # seRequestElements : " + seRequestElements.size());
//        List<SeRequest> filteredSRE = new ArrayList<SeRequest>();
//
//        for (SeRequest seRequestElement : seRequestElements) {
//
//            Log.d(TAG, "Filtering seRequestElement whom protocol : "
//                    + seRequestElement.getProtocolFlag());
//
//            if (seRequestElement.getProtocolFlag() != null
//                    && seRequestElement.getProtocolFlag().equals(tagTransceiver.getTech())) {
//                filteredSRE.add(seRequestElement);
//            }
//        }
//        Log.d(TAG, "After Filter seRequestElement : " + filteredSRE.size());
//        return filteredSRE;
//
//    }



    /**
     * Keep the current channel open for further commands
     */
    private void saveChannelState(ByteBuffer aid) {
        Log.d(TAG, "save application id for further commands");
        previousOpenApplication = aid;

    }//TODO


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
        return tagTransceiver != null
                ? tagTransceiver.getTag().getId() + tagTransceiver.getTag().toString()
                : "null";
    }
}

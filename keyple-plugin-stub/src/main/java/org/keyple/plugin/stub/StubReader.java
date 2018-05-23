/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;


/**
 * Stub Implementation of {@link ProxyReader} To inject custom behaviour use
 * {@link StubSecureElement}
 */
public class StubReader extends AbstractObservableReader implements ConfigurableReader {


    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private boolean isSEPresent = false;
    private ByteBuffer previousOpenApplication = null;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";

    private StubSecureElement currentSE;

    private boolean test_WillTimeout = false;


    @Override
    public String getName() {
        return "StubReader";
    }

    /**
     * Simulated transmit function to {@link StubSecureElement} ; Only seRequest whose protocol flag
     * corresponds to the Secure Element technology are executed; if KeepChannelOpen parameter set
     * to true will abort following seRequestElements
     * 
     * @param seRequestSet : Set of seRequest to be sent
     * @return SeResponseSet : set of resulting seResponse
     */
    @Override
    public SeResponseSet transmit(SeRequestSet seRequestSet) throws IOReaderException {
        logger.info("Calling transmit on Keyple Stub Reader");

        if (seRequestSet == null) {
            throw new IOReaderException("se Request Set should not be null");
        }

        if (!isSEPresent) {
            throw new IOReaderException("Secured Element is not present");
        }

        if (test_WillTimeout) {
            throw new IOReaderException("Timeout while transmitting");
        }

        logger.info("APDU commands are simulated with StubCurrentElement : " + currentSE.getTech());
        logger.info("Size of APDU Requests : " + String.valueOf(seRequestSet.getElements().size()));



        // init response
        List<SeResponse> seResponses = new ArrayList<SeResponse>();

        // Filter requestElements whom protocol matches the current SE
        List<SeRequest> seRequests = filterByProtocol(seRequestSet.getElements());

        // no seRequestElements are left after filtering
        if (seRequests.size() < 1) {
            return new SeResponseSet(seResponses);
        }


        // process the request elements
        for (int i = 0; i < seRequests.size(); i++) {

            logger.info("Processing seRequestElements # " + i);

            SeRequest seRequest = seRequests.get(i);

            // init response
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            ApduResponse fciResponse = null;

            try {

                // Checking of the presence of the AID request in requests group
                ByteBuffer aid = seRequest.getAidToSelect();
                logger.info("Connect to application, aid :" + ByteBufferUtils.toHex(aid));

                // Open the application channel if not open yet
                if (previousOpenApplication == null || previousOpenApplication != aid) {
                    logger.info("Connecting to application : " + aid);
                    fciResponse = this.connectApplication(seRequest.getAidToSelect());
                } else {
                    logger.info("Application was already open : " + aid);
                }

                // Send all apduRequest
                for (ApduRequest apduRequest : seRequest.getApduRequests()) {
                    apduResponses.add(currentSE.process(apduRequest));
                }

                // Add ResponseElements to global SeResponseSet
                SeResponse out =
                        new SeResponse(previousOpenApplication != null, fciResponse, apduResponses);
                seResponses.add(out);

                // Don't process more seRequestElement if asked
                if (seRequest.keepChannelOpen()) {
                    logger.info(
                            "Keep Channel Open is set to true, abort further seRequestElement if any");
                    saveChannelState(aid);
                    break;
                }

                // For last element, close physical channel if asked
                if (i == seRequests.size() - 1 && !seRequest.keepChannelOpen()) {
                    disconnect(currentSE);
                }

            } catch (IOException e) {
                logger.error("Error executing command");
                e.printStackTrace();
                apduResponses.add(null);// add empty response
            }

        }

        return new SeResponseSet(seResponses);
    }



    @Override
    public boolean isSEPresent() throws IOReaderException {
        return isSEPresent;
    }



    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setParameters(Map<String, String> parameters) throws IOReaderException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }

    @Override
    public void setParameter(String name, String value) throws IOReaderException {
        if (name.equals(ALLOWED_PARAMETER_1) || name.equals(ALLOWED_PARAMETER_2)) {
            parameters.put(name, value);
        } else {
            throw new IOReaderException("parameter name not supported : " + name);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }



    /**
     * Physical connect to Secure Element (simulated)
     * 
     * @param se : Stub Secure Element to connect to
     */
    protected void connect(StubSecureElement se) {
        isSEPresent = true;
        currentSE = se;
        logger.info("Connect SE : " + se.getTech());
        notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_INSERTED));
    }

    /**
     * Physical disconnect to Secure Element (simulated)
     * 
     * @param se : Stub Secure Element to connect to
     */
    protected void disconnect(StubSecureElement se) {
        logger.info("Disconnect SE : " + se.getTech());
        isSEPresent = false;
        currentSE = null;
        notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_REMOVAL));
    }

    /**
     * Activate a simulated timeout during transmit command
     * 
     * @param willTimeout
     */
    public void configureWillTimeout(Boolean willTimeout) {
        logger.info("Configure test will timeout to " + willTimeout);
        test_WillTimeout = willTimeout;
    }


    /**
     * Build and send an APDU to select 'aid'
     * 
     * @param aid : aid to select
     * @return : response from SE
     * @throws IOException
     */
    private ApduResponse connectApplication(ByteBuffer aid) throws IOException {

        ByteBuffer command = ByteBuffer.allocate(aid.limit() + 6);
        command.put((byte) 0x00);
        command.put((byte) 0xA4);
        command.put((byte) 0x04);
        command.put((byte) 0x00);
        command.put((byte) aid.limit());
        command.put(aid);
        command.put((byte) 0x00);
        command.position(0);

        logger.info("Select application APDU: " + ByteBufferUtils.toHex(command));
        return currentSE.process(new ApduRequest(command, false));

    }


    /**
     * Filter by protocol the seRequestElements which matches with SE Technology
     * 
     * @param seRequestElements
     * @return filtered list of seRequestElements
     */
    private List<SeRequest> filterByProtocol(List<SeRequest> seRequestElements) {

        logger.info("Filtering # seRequestElements : " + seRequestElements.size());
        List<SeRequest> filteredSRE = new ArrayList<SeRequest>();

        for (SeRequest seRequestElement : seRequestElements) {

            logger.info("Filtering seRequestElement whom protocol : "
                    + seRequestElement.getProtocolFlag());

            if (seRequestElement.getProtocolFlag() != null
                    && seRequestElement.getProtocolFlag().equals(currentSE.getTech())) {
                filteredSRE.add(seRequestElement);
            }
        }
        logger.info("After Filter seRequestElement : " + filteredSRE.size());
        return filteredSRE;
    }


    /**
     * Keep the current channel open for further commands
     */
    private void saveChannelState(ByteBuffer aid) {
        logger.info("Save application id for further commands");
        previousOpenApplication = aid;
    }
}

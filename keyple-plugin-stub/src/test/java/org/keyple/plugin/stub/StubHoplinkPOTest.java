/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;



import static junit.framework.TestCase.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ByteBufferUtils;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeRequestSet;
import org.keyple.seproxy.SeResponseSet;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.Observable;
import org.mockito.junit.MockitoJUnitRunner;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class StubHoplinkPOTest {

    private static final ILogger logger = SLoggerFactory.getLogger(StubHoplinkPOTest.class);

    StubReader stubReader;
    StubSecureElement csc;

    @Before
    public void SetUp() throws IOReaderException {
        stubReader = (StubReader) StubPlugin.getInstance().getReaders().get(0);
        stubReader.configureWillTimeout(false);
        stubReader.clearObservers();
        csc = new StubHoplinkPO();
    }

    /**
     * Test that Reader sends the event {@link org.keyple.seproxy.ReaderEvent.EventType#SE_INSERTED}
     * 
     * @throws IOReaderException
     */
    @Test
    public void InsertCalypso() throws IOReaderException {

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
            }

        });

        csc.insertInto(stubReader);

    }


    /**
     * Test that Reader sends the event {@link org.keyple.seproxy.ReaderEvent.EventType#SE_REMOVAL}
     * 
     * @throws IOReaderException
     */
    @Test
    public void InsertRemoveCalypso() throws IOReaderException {

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            Boolean inserted = false;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                if (!inserted) {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    inserted = true;
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }
            }

        });

        csc.insertInto(stubReader);
        csc.removeFrom(stubReader);

    }


    @Test
    public void selectCalypsoApplication() throws IOReaderException {

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {

            Boolean inserted = false;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {

                if (!inserted) {
                    // ISO SE has been inserted
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    inserted = true;

                    // transmit 3 commands
                    String poAid = "A000000291A000000191";
                    String t2UsageRecord1_dataFill =
                            "0102030405060708090A0B0C0D0E0F10" + "1112131415161718191A1B1C1D1E1F20"
                                    + "2122232425262728292A2B2C2D2E2F30";

                    ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x14, (byte) 0x20);
                    ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);
                    UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
                            new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x01, (byte) 0x1A,
                                    ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

                    // Get PO ApduRequest List
                    List<ApduRequest> poApduRequestList =
                            Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                                    poReadRecordCmd_T2Usage.getApduRequest(),
                                    poUpdateRecordCmd_T2UsageFill.getApduRequest());

                    SeRequest seRequestElement =
                            new SeRequest(ByteBufferUtils.fromHex(poAid), poApduRequestList, false);
                    seRequestElement.setProtocolFlag("android.nfc.tech.IsoDep");
                    List<SeRequest> seRequestElements = new ArrayList<SeRequest>();
                    seRequestElements.add(seRequestElement);
                    SeRequestSet poRequest = new SeRequestSet(seRequestElements);


                    try {
                        SeResponseSet poResponse = stubReader.transmit(poRequest);
                        logger.info("poResponse Elements : " + poResponse.getElements().size());
                        assert (poResponse.getElements().size() == 1);
                        assert (poResponse.getElements().get(0).getApduResponses().size() == 3);
                        assert (poResponse.getElements().get(0).getApduResponses().get(0)
                                .isSuccessful());
                        assert (poResponse.getElements().get(0).getApduResponses().get(1)
                                .isSuccessful());
                        assert (poResponse.getElements().get(0).getApduResponses().get(2)
                                .isSuccessful());
                    } catch (IOReaderException e) {
                        e.printStackTrace();
                        fail("Should not raise exception");
                    }
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }

            }

        });

        csc.insertInto(stubReader);

    }

    @Test
    public void selectUnknowApplication() throws IOReaderException {


        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {

            Boolean inserted = false;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {

                if (!inserted) {
                    // ISO SE has been inserted
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    inserted = true;

                    // Select wrong application and transmit 1 command
                    String poAid = "A0";

                    ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x14, (byte) 0x20);

                    // Get PO ApduRequest List
                    List<ApduRequest> poApduRequestList =
                            Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

                    SeRequest seRequestElement =
                            new SeRequest(ByteBufferUtils.fromHex(poAid), poApduRequestList, false);
                    seRequestElement.setProtocolFlag("android.nfc.tech.IsoDep");
                    List<SeRequest> seRequestElements = new ArrayList<SeRequest>();
                    seRequestElements.add(seRequestElement);
                    SeRequestSet poRequest = new SeRequestSet(seRequestElements);


                    try {
                        SeResponseSet poResponse = stubReader.transmit(poRequest);
                        logger.info("poResponse Elements : " + poResponse.getElements().size());
                        assert (poResponse.getElements().size() == 1);
                        assert (poResponse.getElements().get(0).getApduResponses().size() == 0);
                    } catch (IOReaderException e) {
                        e.printStackTrace();
                        fail("Should not raise exception");
                    }
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }

            }

        });

        csc.insertInto(stubReader);

    }


}

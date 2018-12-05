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
package org.eclipse.keyple.example.android.nfc;


import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcFragment;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Test the Keyple NFC Plugin Configure the NFC reader Configure the Observability Run test commands
 * when appropriate tag is detected.
 */
public class NFCTestFragment extends Fragment implements ObservableReader.ReaderObserver {

    private static final Logger LOG = LoggerFactory.getLogger(NFCTestFragment.class);

    private static final String TAG = NFCTestFragment.class.getSimpleName();
    private static final String TAG_NFC_ANDROID_FRAGMENT =
            "org.eclipse.keyple.plugin.android.nfc.AndroidNfcFragment";

    // UI
    private TextView mText;


    public static NFCTestFragment newInstance() {
        return new NFCTestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android NFC Plugin Add this view to the list of Observer
     * of @{@link SeReader}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1 - First initialize SEProxy with Android Plugin
        LOG.debug("Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> plugins = new ConcurrentSkipListSet<ReaderPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // 2 - add NFC Fragment to activity in order to communicate with Android Plugin
        LOG.debug("Add Keyple NFC Fragment to activity in order to "
                + "communicate with Android Plugin");
        getFragmentManager().beginTransaction()
                .add(AndroidNfcFragment.newInstance(), TAG_NFC_ANDROID_FRAGMENT).commit();


        try {
            // define task as an observer for ReaderEvents
            LOG.debug("Define this view as an observer for ReaderEvents");
            SeReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((AndroidNfcReader) reader).addObserver(this);

            reader.setParameter("FLAG_READER_PRESENCE_CHECK_DELAY", "5000");
            reader.setParameter("FLAG_READER_NO_PLATFORM_SOUNDS", "0");
            reader.setParameter("FLAG_READER_SKIP_NDEF_CHECK", "0");


            // with this protocol settings we activate the nfc for ISO1443_4 protocol
            ((AndroidNfcReader) reader).addSeProtocolSetting(
                    new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));


            /*
             * uncomment to active protocol listening for Mifare ultralight ((AndroidNfcReader)
             *
             * reader).addSeProtocolSetting( AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_UL);
             *
             * uncomment to active protocol listening for Mifare Classic ((AndroidNfcReader)
             * reader).addSeProtocolSetting(
             * AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC);
             */

        } catch (KeypleBaseException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialize UI for NFC Test view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Define UI components
        View view =
                inflater.inflate(org.eclipse.keyple.example.android.nfc.R.layout.fragment_nfc_test,
                        container, false);
        mText = view.findViewById(org.eclipse.keyple.example.android.nfc.R.id.text);
        initTextView();
        return view;
    }

    /**
     * Catch @{@link AndroidNfcReader} events When a SE is inserted, launch test commands
     **
     * @param event
     */
    @Override
    public void update(final ReaderEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LOG.info("New ReaderEvent received : " + event.toString());

                switch (event.getEventType()) {
                    case SE_INSERTED:

                        // execute simple tests
                        runHoplinkSimpleRead();
                        break;

                    case SE_REMOVAL:
                        // mText.append("\n ---- \n");
                        // mText.append("Connection closed to tag");
                        break;

                    case IO_ERROR:
                        mText.append("\n ---- \n");
                        mText.setText("Error reading card");
                        break;

                }
            }
        });
    }


    /**
     * Run Hoplink Simple read command
     */
    private void runHoplinkSimpleRead() {
        LOG.debug("Running HopLink Simple Read Tests");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    initTextView();

                    SeReader reader = null;
                    reader = SeProxyService.getInstance().getPlugins().first().getReaders().first();

                    /*
                     * print tag info in View
                     */
                    mText.append("\n ---- \n");
                    mText.append(((AndroidNfcReader) reader).printTagId());
                    mText.append("\n ---- \n");

                    /*
                     * Prepare a Calypso PO selection
                     */
                    SeSelection seSelection = new SeSelection(reader);

                    byte[] poAid = ByteArrayUtils.fromHex(CalypsoClassicInfo.AID);

                    /*
                     * Setting up of an AID based selection of a Calypso REV3 PO
                     *
                     * Select the first application matching the selection AID whatever the SE
                     * communication protocol keep the logical channel open after the selection
                     */

                    /*
                     * Calypso selection: configures a PoSelector with all the desired attributes to
                     * make the selection and read additional information afterwards
                     */
                    PoSelector poSelector = new PoSelector(poAid, SeSelector.SelectMode.FIRST,
                            ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4,
                            "AID: " + CalypsoClassicInfo.AID);

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the selection has been made.
                     */
                    ReadRecordsRespPars readEnvironmentParser = poSelector.prepareReadRecordsCmd(
                            CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                            ReadDataStructure.SINGLE_RECORD_DATA,
                            CalypsoClassicInfo.RECORD_NUMBER_1,
                            String.format("EnvironmentAndHolder (SFI=%02X))",
                                    CalypsoClassicInfo.SFI_EnvironmentAndHolder));

                    appendColoredText(mText, "\n1st PO exchange:\n", Color.BLACK);
                    mText.append(" * select a Calypso PO\n * read the environment file\n");

                    /*
                     * Add the selection case to the current selection (we could have added other
                     * cases here)
                     */
                    CalypsoPo calypsoPo = (CalypsoPo) seSelection.prepareSelection(poSelector);

                    /*
                     * Actual PO communication: operate through a single request the Calypso PO
                     * selection and the file read
                     */
                    if (seSelection.processExplicitSelection()) {
                        mText.append("\nCalypso PO selection: ");
                        appendColoredText(mText, "SUCCESS\n", Color.GREEN);
                        mText.append("AID: ");
                        appendHexBuffer(mText, poAid);

                        /*
                         * Retrieve the data read from the parser updated during the selection
                         * process
                         */
                        byte environmentAndHolder[] = (readEnvironmentParser.getRecords())
                                .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                        mText.append("\n\nEnvironment and Holder file: ");
                        appendHexBuffer(mText, environmentAndHolder);

                        appendColoredText(mText, "\n\n2nd PO exchange:\n", Color.BLACK);
                        mText.append("* read the event log file");
                        PoTransaction poTransaction = new PoTransaction(reader, calypsoPo);

                        /*
                         * Prepare the reading order and keep the associated parser for later use
                         * once the transaction has been processed.
                         */
                        ReadRecordsRespPars readEventLogParser =
                                poTransaction.prepareReadRecordsCmd(CalypsoClassicInfo.SFI_EventLog,
                                        ReadDataStructure.SINGLE_RECORD_DATA,
                                        CalypsoClassicInfo.RECORD_NUMBER_1,
                                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                                CalypsoClassicInfo.SFI_EventLog,
                                                CalypsoClassicInfo.RECORD_NUMBER_1));

                        /*
                         * Actual PO communication: send the prepared read order, then close the
                         * channel with the PO
                         */
                        if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                            mText.append("\nTransaction: ");
                            appendColoredText(mText, "SUCCESS\n", Color.GREEN);

                            /*
                             * Retrieve the data read from the parser updated during the transaction
                             * process
                             */
                            byte eventLog[] = (readEventLogParser.getRecords())
                                    .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                            /* Log the result */
                            mText.append("\nEventLog file:\n");
                            appendHexBuffer(mText, eventLog);
                        }
                        appendColoredText(mText, "\n\nEnd of the Calypso PO processing.",
                                Color.BLACK);
                    } else {
                        mText.append("\nCalypso PO selection: ");
                        appendColoredText(mText, "\nFAILURE\n", Color.RED);
                        mText.append("AID: ");
                        appendHexBuffer(mText, poAid);
                        mText.append("\n\nThe selection of the PO has failed.");
                    }
                } catch (Exception e) {
                    LOG.debug("Exception: " + e.getMessage());
                    appendColoredText(mText, "Exception: " + e.getMessage(), Color.RED);
                    e.fillInStackTrace();
                }
            }

        });

    }



    /**
     * Revocation of the Activity from @{@link AndroidNfcReader} list of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            LOG.debug("Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            SeReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((ObservableReader) reader).removeObserver(this);

            // destroy AndroidNFC fragment
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialize display
     */
    private void initTextView() {
        mText.setText("");// reset
        appendColoredText(mText, "Waiting for a smartcard...", Color.BLUE);
        mText.append("\n ---- \n");
    }

    /**
     * Append to tv a string containing an hex representation of the byte array provided in
     * argument.
     * <p>
     * The font used is monospaced.
     * 
     * @param tv TextView
     * @param ba byte array
     */
    private static void appendHexBuffer(TextView tv, byte[] ba) {
        int start = tv.getText().length();
        tv.append(ByteArrayUtils.toHex(ba));
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();

        spannableText.setSpan(new TypefaceSpan("monospace"), start, end, 0);
        spannableText.setSpan(new RelativeSizeSpan(0.70f), start, end, 0);
    }

    /**
     * Append to tv a text colored according to the provided argument
     * 
     * @param tv TextView
     * @param text string
     * @param color color value
     */
    private static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.androidnfc;


import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.example.common.HoplinkSimpleRead;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcProtocolSettings;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcReader;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractLoggedObservable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.util.Observable;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;


public class NFCTestFragment extends Fragment
        implements AbstractLoggedObservable.Observer<ReaderEvent> {


    private static final String TAG = NFCTestFragment.class.getSimpleName();
    private static final String TAG_NFC_ANDROID_FRAGMENT =
            "org.eclipse.keyple.plugin.androidnfc.AndroidNfcFragment";

    // UI
    private TextView mText;
    private RadioGroup radioGroup;


    public static NFCTestFragment newInstance() {
        return new NFCTestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android NFC Plugin Add this view to the list of Observer
     * of @{@link ProxyReader}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1 - First initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // 2 - add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add Keyple NFC Fragment to activity in order to "
                + "communicate with Android Plugin");
        getFragmentManager().beginTransaction()
                .add(AndroidNfcFragment.newInstance(), TAG_NFC_ANDROID_FRAGMENT).commit();


        try {
            // define task as an observer for ReaderEvents
            Log.d(TAG, "Define this view as an observer for ReaderEvents");
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((AndroidNfcReader) reader).addObserver(this);


            ((AndroidNfcReader) reader).addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4);
            ((AndroidNfcReader) reader).addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC);

        } catch (IOReaderException e) {
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


        View view = inflater.inflate(R.layout.fragment_nfc_test, container, false);

        mText = view.findViewById(R.id.text);
        radioGroup = view.findViewById(R.id.radioGroup);

        return view;


    }


    /**
     * Run commands test
     */
    private void runTest() {

        if (radioGroup.getCheckedRadioButtonId() == R.id.hoplinkSimpleRead) {
            runHoplinkSimpleRead();
        }
    }

    /**
     * Run Hoplink Simple read command
     */
    private void runHoplinkSimpleRead() {
        Log.d(TAG, "Running HopLink Simple Read Tests");
        ProxyReader reader = null;
        try {
            reader = SeProxyService.getInstance().getPlugins().get(0).getReaders().get(0);

            SeRequest seRequest = new SeRequest(HoplinkSimpleRead.getAid(),
                    HoplinkSimpleRead.getApduList(),
                    false,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);


            SeResponseSet seResponseSet =
                    reader.transmit(new SeRequestSet(seRequest));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mText.append("\n ---- \n");
                    for (SeResponse response : seResponseSet.getResponses()) {
                        if (response != null) {
                            for (ApduResponse apdu : response.getApduResponses()) {
                                mText.append("Response : " + apdu.getStatusCode() + " - "
                                        + ByteBufferUtils.toHex(apdu.getDataOut()));
                                mText.append("\n");
                            }
                        } else {
                            mText.append("Response : null");
                            mText.append("\n");
                        }
                    }
                }
            });

        } catch (IOReaderException e) {
            e.printStackTrace();
        }


    }



    /**
     * Revocation of the Activity
     * from @{@link org.eclipse.keyple.plugin.androidnfc.AndroidNfcReader} list of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((AbstractObservableReader) reader).removeObserver(this);

            // destroy AndroidNFC fragment
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


    /**
     * Catch @{@link org.eclipse.keyple.plugin.androidnfc.AndroidNfcReader} events When a SE is
     * inserted, launch test commands
     * 
     * @param observable
     * @param event
     */
    @Override
    public void update(Observable<ReaderEvent> observable, ReaderEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " + event.toString());

                switch (event) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Tag opened to tag");
                        try {

                            runTest();

                        } catch (InconsistentCommandException e) {
                            e.printStackTrace();
                        }
                        break;

                    case SE_REMOVAL:
                        mText.append("\n ---- \n");
                        mText.append("Connection closed to tag");
                        break;

                    case IO_ERROR:
                        mText.append("\n ---- \n");
                        mText.setText("Error reading card");
                        break;

                }
            }
        });
    }
}

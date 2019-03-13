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
package org.eclipse.keyple.integration.tools.calypso;

public class PoFile {

    // File Type Values
    public final static int FILE_TYPE_MF = 1;
    public final static int FILE_TYPE_DF = 2;
    public final static int FILE_TYPE_EF = 4;

    // EF Type Values
    public final static int EF_TYPE_DF = 0;
    public final static int EF_TYPE_BINARY = 1;
    public final static int EF_TYPE_LINEAR = 2;
    public final static int EF_TYPE_CYCLIC = 4;
    public final static int EF_TYPE_SIMULATED_COUNTERS = 8;
    public final static int EF_TYPE_COUNTERS = 9;

    private byte[] fileBinaryData = null;

    private int lid = 0;

    private byte sfi = 0;

    private byte fileType = 0;

    private byte efType = 0;

    private int recSize = 0;

    private byte numRec = 0;

    private byte[] accessConditions = null;

    private byte[] keyIndexes = null;

    private byte simulatedCounterFileSfi = 0;

    private byte simulatedCounterNumber = 0;

    private int sharedEf = 0;

    private byte dfStatus = 0;

    private byte[] rfu = null;

    private byte[] kvcInfo = null;

    private byte[] kifInfo = null;

    public PoFile(byte[] inFileParameters) {

        int iter = 0;

        if(inFileParameters[iter++] != 0x85) {  // Check File TLV Tag
            return;
        }

        if(inFileParameters[iter++] != 0x17) {  // Check File TLV Length
            return;
        }

        fileBinaryData = new byte[inFileParameters.length];
        System.arraycopy(inFileParameters, 0, fileBinaryData, 0, inFileParameters.length);

        sfi = inFileParameters[iter++];
        fileType = inFileParameters[iter++];
        efType = inFileParameters[iter++];

        if(fileType == FILE_TYPE_EF && efType == EF_TYPE_BINARY) {

            recSize = ((inFileParameters[iter+1]<< 8)&0x0000ff00) | (inFileParameters[iter]&0x000000ff);
            numRec = 1;
            iter += 2;

        } else if(fileType == FILE_TYPE_EF) {

            recSize = inFileParameters[iter++];
            numRec = inFileParameters[iter++];
        }

        accessConditions = new byte[4];
        System.arraycopy(inFileParameters, iter, accessConditions, 0, 4);
        iter += 4;

        keyIndexes = new byte[4];
        System.arraycopy(inFileParameters, iter, keyIndexes, 0, 4);
        iter += 4;

        dfStatus = inFileParameters[iter];

        if(fileType == FILE_TYPE_EF) {

            if(efType == EF_TYPE_SIMULATED_COUNTERS) {

                simulatedCounterFileSfi = inFileParameters[iter++];
                simulatedCounterNumber = inFileParameters[iter++];

            } else {

                sharedEf = ((inFileParameters[iter+1]<< 8)&0x0000ff00) | (inFileParameters[iter]&0x000000ff);
                iter += 2;
            }

            rfu = new byte[5];
            System.arraycopy(inFileParameters, iter, rfu, 0, 5);
            iter += 5; // RFU fields;

        } else {

            kvcInfo = new byte[3];
            System.arraycopy(inFileParameters, iter, kvcInfo, 0, 4);
            iter += 3;

            kifInfo = new byte[3];
            System.arraycopy(inFileParameters, iter, kifInfo, 0, 4);
            iter += 3;


            rfu = new byte[1];
            rfu[0] = inFileParameters[iter++];
        }

        lid = ((inFileParameters[iter+1]<< 8)&0x0000ff00) | (inFileParameters[iter]&0x000000ff);
    }


    public int getLid() {
        return lid;
    }

    public byte getSfi() {
        return sfi;
    }

    public byte getFileType() {
        return fileType;
    }

    public byte getEfType() {
        return efType;
    }

    public int getRecSize() {
        return recSize;
    }

    public byte getNumRec() {
        return numRec;
    }

    public byte[] getAccessConditions() {
        return accessConditions;
    }

    public byte[] getKeyIndexes() {
        return keyIndexes;
    }

    public byte getSimulatedCounterFileSfi() {
        return simulatedCounterFileSfi;
    }

    public byte getSimulatedCounterNumber() {
        return simulatedCounterNumber;
    }

    public int getSharedEf() {
        return sharedEf;
    }

    public byte getDfStatus() {
        return dfStatus;
    }

    public byte[] getFileBinaryData() {
        return fileBinaryData;
    }

    public byte[] getRfu() {
        return rfu;
    }

    public byte[] getKvcInfo() {
        return kvcInfo;
    }

    public byte[] getKifInfo() {
        return kifInfo;
    }
}

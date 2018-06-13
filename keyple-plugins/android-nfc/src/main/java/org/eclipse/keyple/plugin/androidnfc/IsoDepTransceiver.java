/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import java.io.IOException;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

/** IsoDep Implementation of {@link org.eclipse.keyple.plugin.androidnfc.TagTransceiver} */
class IsoDepTransceiver extends TagTransceiver {

    private final IsoDep tag;

    @Override
    public int getMaxTransceiveLength() {
        return tag.getMaxTransceiveLength();
    }

    @Override
    public String getTech() {
        return AndroidNfcProtocolSettings.TAG_TECHNOLOGY_ISO14443_4;
    }

    IsoDepTransceiver(Tag tag) {
        this.tag = IsoDep.get(tag);
    }


    @Override
    public byte[] transceive(byte[] data) throws IOException {
        return tag.transceive(data);
    }

    @Override
    public Tag getTag() {
        return tag.getTag();
    }

    @Override
    public void connect() throws IOException {
        tag.connect();
    }

    @Override
    public void close() throws IOException {
        tag.close();
    }

    @Override
    public boolean isConnected() {
        return tag.isConnected();
    }
}

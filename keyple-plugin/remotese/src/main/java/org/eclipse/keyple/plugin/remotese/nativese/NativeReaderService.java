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
package org.eclipse.keyple.plugin.remotese.nativese;

import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;

public interface NativeReaderService extends ObservableReader.ReaderObserver {


    /**
     * Connect Physical Local Reader to Remote SE Creates a Session to exchange data with this
     * Reader with an option to duplex connection
     */
    void connectReader(ProxyReader localReader, String clientNodeId);

    void disconnectReader(ProxyReader localReader, String clientNodeId);

    /**
     * Find a local reader acroos plugins by its name
     * @param nativeReaderName : native name of the reader to find
     * @return proxy reader if found
     */
    public ProxyReader findLocalReader(String nativeReaderName)  throws KeypleReaderNotFoundException;


}

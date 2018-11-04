/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.example.pc.generic;


import org.eclipse.keyple.example.common.generic.ObservableReaderNotificationEngine;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.*;


public class Demo_ObservableReaderNotification_Pcsc {
    public final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

        /* Instantiate SeProxyService and add PC/SC plugin */
        SeProxyService seProxyService = SeProxyService.getInstance();

        seProxyService.addPlugin(PcscPlugin.getInstance());

        /* Set observers */
        demoEngine.setPluginObserver();

        System.out.println("Wait for reader or SE insertion/removal");

        /* Wait indefinitely. CTRL-C to exit. */
        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
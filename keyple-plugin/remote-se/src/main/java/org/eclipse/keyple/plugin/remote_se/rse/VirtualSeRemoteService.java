/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import java.util.SortedSet;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;

/**
 * Service to setDtoSender a RSE Plugin to a Transport Node
 */
public class VirtualSeRemoteService {

    private DtoSender node;
    private final SeProxyService seProxyService;

    public VirtualSeRemoteService() {
        this.seProxyService = SeProxyService.getInstance();
    }

    /**
     * Bind TransportNode to VirtualSeRemoteService
     * 
     * @param node
     */
    public void setDtoSender(DtoSender node) {
        this.node = node;
    }

    /**
     * Bind plugin to VirtualSeRemoteService
     * 
     * @param plugin
     */
    public void registerRsePlugin(RsePlugin plugin) {
        SortedSet<ReaderPlugin> plugins = seProxyService.getPlugins();
        plugins.add(plugin);
        seProxyService.setPlugins(plugins);
        // this.node.setDtoDispatcher(plugin); done by Master class
    }


    // manage session


}
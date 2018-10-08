/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.common;

import org.eclipse.keyple.plugin.remote_se.transport.DtoDispatcher;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;

/**
 * TransportNode is a gateway for incoming and outgoing TransportDto
 */
public interface TransportNode extends DtoSender {

    void setDtoDispatcher(DtoDispatcher receiver);

}
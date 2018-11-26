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
package org.eclipse.keyple.plugin.remotese.pluginse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualReaderServiceImplTest {


    SeProxyService seProxyService;

    @Mock
    DtoSender dtoSender;

    @Mock
    DtoHandler dtoHandler;

    @Mock
    TransportNode transportNode; // object that links a dtoSender and a dtoHanlder

    VirtualReaderService vre;


    @Before
    public void setTup() {

        MockitoAnnotations.initMocks(this);

        // mock
        doNothing().when(transportNode).setDtoHandler(any(DtoHandler.class));

        seProxyService = SeProxyService.getInstance();

        // init VRE
        vre = new VirtualReaderService(seProxyService, dtoSender);
        vre.bindDtoEndpoint(transportNode);
    }

    @Test
    public void testGetPlugin() {
        RemoteSePlugin plugin = vre.getPlugin();
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testTransmitException() {
        KeypleDto keypleDto = KeypleDtoHelper.ExceptionDTO(RemoteMethod.READER_TRANSMIT.getName(),
                new KeypleReaderException("test"), "any", "any", "any", "any");

    }

}

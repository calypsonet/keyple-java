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

import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.TransportNode;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class NativeReaderServiceImplTest {


    @Mock
    DtoSender dtoSender;

    @Mock
    TransportNode transportNode;

    private NativeReaderServiceImpl nre;

    @Before
    public void Setup() {
        MockitoAnnotations.initMocks(this);


        doNothing().when(transportNode).setDtoHandler(any(DtoHandler.class));

        //Prepare nre with Mock
        nre = new NativeReaderServiceImpl(dtoSender);
        nre.bindDtoEndpoint(transportNode);
    }



    @Test
    public void TestConnectReader(){
        //todo

    }

}

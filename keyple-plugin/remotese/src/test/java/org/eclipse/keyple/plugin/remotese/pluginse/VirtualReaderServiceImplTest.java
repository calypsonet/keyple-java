package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportDto;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class VirtualReaderServiceImplTest {


    SeProxyService seProxyService;

    @Mock
    DtoSender dtoSender;

    @Mock
    DtoHandler dtoHandler;

    @Mock
    TransportNode transportNode; //object that links a dtoSender and a dtoHanlder

    VirtualReaderService vre;


    @Before
    public void setTup(){

        MockitoAnnotations.initMocks(this);

        //mock
        doNothing().when(transportNode).setDtoHandler(any(DtoHandler.class));

        seProxyService = SeProxyService.getInstance();

        //init VRE
        vre = new VirtualReaderService(seProxyService, dtoSender);
        vre.bindDtoEndpoint(transportNode);
    }

    @Test
    public void testGetPlugin(){
        RemoteSePlugin plugin =  vre.getPlugin();
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testTransmitException(){
        KeypleDto keypleDto = KeypleDtoHelper.ExceptionDTO(RemoteMethod.READER_TRANSMIT.getName(), new KeypleReaderException("test"),
                "any", "any", "any", "any");

    }

}

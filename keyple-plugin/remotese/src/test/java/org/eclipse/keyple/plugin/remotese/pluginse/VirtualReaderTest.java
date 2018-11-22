package org.eclipse.keyple.plugin.remotese.pluginse;


import org.eclipse.keyple.seproxy.SeRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualReaderTest {


    @Mock
    VirtualReaderSession session;

    VirtualReader reader;

    @Before
    public void setTup(){
        MockitoAnnotations.initMocks(this);

        reader = new VirtualReader(session, "nativeReader");

    }


    @Test
    public void testNullTransmit() throws Exception{

        reader.transmit(null);

    }


}

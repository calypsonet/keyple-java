package org.eclipse.keyple.plugin.remotese.common.json;


import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class KeypleDtoHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(KeypleDtoHelperTest.class);

    @Test
    public void testContainsException() {

        Exception ex = new KeypleReaderException("keyple Reader Exception message", new IOException("error io"));
        KeypleDto dtoWithException = KeypleDtoHelper.ExceptionDTO("any", ex, "any", "any", "any", "any");
        logger.debug(KeypleDtoHelper.toJson(dtoWithException));
        assert KeypleDtoHelper.containsException(dtoWithException);


    }

}

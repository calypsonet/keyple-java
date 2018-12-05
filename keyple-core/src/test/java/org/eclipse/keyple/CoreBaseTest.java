package org.eclipse.keyple;

import org.eclipse.keyple.seproxy.SeProxyServiceTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CoreBaseTest.class);

    @Rule
    public TestName name = new TestName();

}

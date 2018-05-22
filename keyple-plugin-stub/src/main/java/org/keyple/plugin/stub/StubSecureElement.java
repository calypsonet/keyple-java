package org.keyple.plugin.stub;

import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;

import java.nio.ByteBuffer;

public interface StubSecureElement {

    public String getTech();

    public void insertInto(StubReader poReader);
    public void removeFrom(StubReader poReader);

    public ApduResponse process(ApduRequest request);

    public void addCommand(String request, String response);
    public void removeCommand(String request, String response);

}

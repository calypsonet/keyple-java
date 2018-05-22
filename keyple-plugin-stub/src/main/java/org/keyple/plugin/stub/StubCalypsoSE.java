package org.keyple.plugin.stub;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;

import java.util.HashMap;
import java.util.Map;

public class StubCalypsoSE implements StubSecureElement {

    private static final ILogger logger = SLoggerFactory.getLogger(StubCalypsoSE.class);


    String tech = "android.nfc.tech.IsoDep";
    public static String poAid = "A000000291A000000191";

    private Map<String, String> commands;


    public StubCalypsoSE() {

        commands = new HashMap<String, String>();
        commands.put("00A404000A"+poAid+"00","6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");
        commands.put("00B201A420","00000000000000000000000000000000000000000000000000000000000000009000");
        commands.put("00B201D430","0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000");
        commands.put("00DC01D4300102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F30","9000");

    }

    @Override
    public void insertInto(StubReader poReader) {
        poReader.connect(this);
    }

    @Override
    public void removeFrom(StubReader poReader) {
        poReader.disconnect(this);
    }

    @Override
    public String getTech() {
        return tech;
    }

    @Override
    public ApduResponse process(ApduRequest request) {

        String out = null;
        Boolean success = null;
        String commandHex = ByteBufferUtils.toHex(request.getBuffer());
        logger.info("Processing command : " + commandHex);

        if(commands.containsKey(commandHex)){
            out = commands.get(commandHex);
            success= true;
        }else{
            success = false;
        }
        logger.info("Result command : " + out);

        return new ApduResponse(ByteBufferUtils.fromHex(out), success);

    }

    @Override
    public void addCommand(String request, String response) {
        commands.put(request,response);
    }

    @Override
    public void removeCommand(String request, String response) {
        commands.remove(request);
    }


}

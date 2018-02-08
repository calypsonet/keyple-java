package org.keyple.commands.po.builder;

import org.keyple.commands.CalypsoCommands;
import org.keyple.commands.dto.CalypsoRequest;
import org.keyple.commands.po.PoCommandBuilder;
import org.keyple.commands.po.PoRevision;
import org.keyple.commands.po.SendableInSession;
import org.keyple.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * This class implements SendableInSession, it provides the dedicated
 * constructor to build the Get data APDU commands.
 *
 *
 * @author Ixxi
 */
public class GetDataFciCmdBuild extends PoCommandBuilder implements SendableInSession {

    private static CalypsoCommands command = CalypsoCommands.PO_GET_DATA_FCI;

    /**
     * Instantiates a new GetDataFciCmdBuild.
     *
     * @param revision
     *            the PO revison
     */
    public GetDataFciCmdBuild(PoRevision revision) {
        super(command, null);
        byte cla = PoRevision.REV2_4.equals(revision) ? (byte) 0x94 : (byte) 0x00;
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, (byte) 0x00, (byte) 0x6F, null, (byte) 0x00);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

    public GetDataFciCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

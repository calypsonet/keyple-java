package org.keyple.commands.csm.builder;

import org.keyple.commands.CalypsoCommands;
import org.keyple.commands.csm.CsmCommandBuilder;
import org.keyple.commands.csm.CsmRevision;
import org.keyple.commands.dto.CalypsoRequest;
import org.keyple.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * This class provides the dedicated constructor to build the CSM Digest Close
 * APDU command.
 *
 * @author Ixxi
 *
 */
public class DigestCloseCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.CSM_DIGEST_CLOSE;

    /**
     * Instantiates a new DigestCloseCmdBuild .
     *
     * @param revision
     *            of the CSM(SAM)
     * @param expectedResponseLength
     *            the expected response length
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestCloseCmdBuild(CsmRevision revision, byte expectedResponseLength) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new InconsistentCommandException();
        }

        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, null, expectedResponseLength);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

    /**
     * Instantiates a new digest close cmd build.
     *
     * @param request
     *            the request
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestCloseCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(CalypsoCommands.PO_APPEND_RECORD, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

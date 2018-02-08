package org.keyple.commands.csm.builder;

import org.keyple.commands.CalypsoCommands;
import org.keyple.commands.csm.CsmCommandBuilder;
import org.keyple.commands.csm.CsmRevision;
import org.keyple.commands.dto.CalypsoRequest;
import org.keyple.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Digest Update
 * APDU command. This command have to be sent twice for each command executed
 * during a session. First time for the command sent and second time for the
 * answer received
 *
 * @author Ixxi
 *
 */
public class DigestUpdateCmdBuild extends CsmCommandBuilder {

    /** The command reference. */

    private static CalypsoCommands command = CalypsoCommands.CSM_DIGEST_UPDATE;

    /**
     * Instantiates a new DigestUpdateCmdBuild.
     *
     * @param revision
     *            of the CSM(SAM)
     * @param encryptedSession
     *            the encrypted session
     * @param digestData
     *            all bytes from command sent by the PO or response from the
     *            command
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestUpdateCmdBuild(CsmRevision revision, boolean encryptedSession, byte[] digestData)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;
        if (encryptedSession) {
            p2 = (byte) 0x80;
        }

        if (digestData != null && digestData.length > 255) {
            throw new InconsistentCommandException();
        }

        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, digestData);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

    /**
     * Instantiates a new digest update cmd build.
     *
     * @param request
     *            the request
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestUpdateCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class ByteBufferUtils {

    /**
     * Chars we will ignore when loading a sample HEX string. It allows to copy/paste the specs APDU
     */
    private static final Pattern HEX_IGNORED_CHARS = Pattern.compile(" |h");

    /**
     * Create a {@link ByteBuffer} from an hexa string. This method allows spaces and "h".
     * 
     * @param hex Hexa string
     * @return ByteBuffer
     * @throws DecoderException If the buffer is not correctly formatted
     */
    public static ByteBuffer fromHex(String hex) throws DecoderException {
        return ByteBuffer.wrap(Hex.decodeHex(HEX_IGNORED_CHARS.matcher(hex).replaceAll("")));
    }
}

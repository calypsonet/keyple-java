/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.plugin.pcsc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.exceptions.IOReaderException;

public final class PcscPlugin implements ReadersPlugin {

    /** singleton instance of SeProxyService */
    private static PcscPlugin uniqueInstance = new PcscPlugin();


    static final Logger logger = LogManager.getLogger(PcscPlugin.class);

    private TerminalFactory factory = TerminalFactory.getDefault();

    private Map<String, ProxyReader> readers;


    private PcscPlugin() {
        this.readers = new HashMap<String, ProxyReader>();
    }

    /**
     * Gets the single instance of PcscPlugin.
     *
     * @return single instance of PcscPlugin
     */
    public static PcscPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public String getName() {
        return "PcscPlugin";
    }

    @Override
    public List<ProxyReader> getReaders() throws IOReaderException {
        CardTerminals terminals = getCardTerminals();

        if (terminals == null) {
            logger.error("Not terminals found", new Throwable());
            throw new IOReaderException("Not terminals found", new Throwable());
        }
        try {
            if (this.readers.isEmpty()) {
                for (CardTerminal terminal : terminals.list()) {
                    PcscReader reader = new PcscReader(terminal, terminal.getName());
                    if (!this.readers.containsKey(reader.getName())) {
                        this.readers.put(reader.getName(), reader);
                    }
                }
            }
        } catch (CardException e) {
            logger.error("Terminal List not accessible", e);
            throw new IOReaderException(e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error("Terminal List not accessible", e);
            throw new IOReaderException(e.getMessage(), e);
        }

        return new ArrayList<ProxyReader>(this.readers.values());
    }

    public CardTerminals getCardTerminals() {
        return this.factory.terminals();
    }

}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exception;

/**
 * Used when a generic checked occurs in plugin
 */
public class KeyplePluginNotFoundException extends KeyplePluginException {

    /**
     * Exception thrown when Reader is not found
     * 
     * @param pluginName : pluginName that has not been found
     */
    public KeyplePluginNotFoundException(String pluginName) {
        super("Plugin with name " + pluginName + " was not found");
    }


}
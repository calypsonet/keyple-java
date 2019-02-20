/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.transport.json;

import java.lang.reflect.Type;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import com.google.gson.*;

class GsonSelectorTypeAdapter implements JsonDeserializer<SeSelector>, JsonSerializer<SeSelector> {

    @Override
    public JsonElement serialize(SeSelector src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.getAtrFilter() == null) {
            return new JsonPrimitive("aidselector::"
                    + ByteArrayUtils.toHex((src.getAidSelector().getAidToSelect())));
        } else {
            return new JsonPrimitive("atrfilter::" + (src.getAtrFilter().getAtrRegex()));
        }
    }

    @Override
    public SeSelector deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        String element = json.getAsString();
        if (element.startsWith("atrfilter::")) {
            String regex = element.replace("atrfilter::", "");
            return new SeSelector(null, new SeSelector.AtrFilter(regex), null);
        } else {
            if (element.startsWith("aidselector::")) {
                String aidToSelect = element.replace("aidselector::", "");
                return new SeSelector(
                        new SeSelector.AidSelector(ByteArrayUtils.fromHex(aidToSelect), null), null,
                        null);
            } else {
                throw new JsonParseException("Selector malformed");
            }
        }
    }
}

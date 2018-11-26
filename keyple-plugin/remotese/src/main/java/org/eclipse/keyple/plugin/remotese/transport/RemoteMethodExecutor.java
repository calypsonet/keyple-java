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
package org.eclipse.keyple.plugin.remotese.transport;


abstract public class RemoteMethodExecutor {

    abstract public TransportDto execute(TransportDto transportDto);

    // protected TransportDto isSeRequestToSendBack(TransportDto transportDto) {
    // TransportDto out = null;
    // try {
    // // retrieve reader by session
    // VirtualReader virtualReader = (VirtualReader) plugin
    // .getReaderByRemoteName(transportDto.getKeypleDTO().getNativeReaderName());
    //
    // if ((virtualReader.getSession()).hasSeRequestSet()) {
    //
    // // send back seRequestSet
    // out = transportDto.nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
    // JsonParser.getGson().toJson((virtualReader.getSession()).getSeRequestSet()),
    // true, virtualReader.getSession().getSessionId()));
    // } else {
    // // no response
    // out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
    // }
    //
    // } catch (KeypleReaderNotFoundException e) {
    // KeypleDto keypleDto = transportDto.getKeypleDTO();
    // out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(), e,
    // keypleDto.getSessionId(),
    // keypleDto.getNativeReaderName(),keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
    // }
    //
    // return out;
    // }


}

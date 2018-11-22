package org.eclipse.keyple.plugin.remotese.transport;

import java.util.HashMap;
import java.util.Map;

public enum RemoteMethod{

    READER_TRANSMIT("reader_transmit"),
    READER_CONNECT("reader_connect"),
    READER_DISCONNECT("reader_disconnect"),
    READER_EVENT("reader_event");

    private String name;

    RemoteMethod(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }


    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, RemoteMethod> lookup = new HashMap();

    //Populate the lookup table on loading time
    static
    {
        for(RemoteMethod env : RemoteMethod.values())
        {
            lookup.put(env.getName(), env);
        }
    }

    //This method can be used for reverse lookup purpose
    public static RemoteMethod get(String url)
    {
        return lookup.get(url);
    }
}

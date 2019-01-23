package org.eclipse.keyple.example.remote.transport.wspolling.server;

import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PublishQueueManager {

    private final Logger logger = LoggerFactory.getLogger(PublishQueueManager.class);


    final Map<String, KeypleDtoPublishQueue<KeypleDto>> queues;

    public PublishQueueManager(){

        logger.info("Initialize PublishQueueManager");
        queues = new HashMap<String, KeypleDtoPublishQueue<KeypleDto>>();
    }

    public KeypleDtoPublishQueue create(String webClientId){
        logger.debug("Create a KeypleDtoPublishQueue for webClientId {}", webClientId);
        if(webClientId==null){
            throw  new IllegalArgumentException("webClientId must not be null");
        }
        KeypleDtoPublishQueue<KeypleDto> queue = new KeypleDtoPublishQueue<KeypleDto>(webClientId);
        queues.put(webClientId, queue);
        return queues.get(webClientId);
    }

    public KeypleDtoPublishQueue get(String webClientId){
        if(webClientId==null){
            throw  new IllegalArgumentException("webClientId must not be null");
        }
        return queues.get(webClientId);
    }

    public void delete(String webClientId){
        if(webClientId==null){
            throw  new IllegalArgumentException("webClientId must not be null");
        }
        queues.remove(webClientId);
    }

    public Boolean exists(String webClientId){
        if(webClientId==null){
            throw  new IllegalArgumentException("webClientId must not be null");
        }
        return queues.containsKey(webClientId);
    }

}

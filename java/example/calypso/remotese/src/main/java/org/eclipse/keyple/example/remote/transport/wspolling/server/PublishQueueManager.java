package org.eclipse.keyple.example.remote.transport.wspolling.server;

import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PublishQueueManager {

    private final Logger logger = LoggerFactory.getLogger(PublishQueueManager.class);


    final Map<String, PublishQueue<KeypleDto>> queues;

    public PublishQueueManager(){

        logger.info("Initialize PublishQueueManager");
        queues = new HashMap<String, PublishQueue<KeypleDto>>();
    }

    public PublishQueue create(String webClientId){
        logger.debug("Create a PublishQueue for webClientId {}", webClientId);
        if(webClientId==null){
            throw  new IllegalArgumentException("webClientId must not be null");
        }
        PublishQueue<KeypleDto> queue = new PublishQueue<KeypleDto>(webClientId);
        queues.put(webClientId, queue);
        return queues.get(webClientId);
    }

    public PublishQueue get(String webClientId){
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

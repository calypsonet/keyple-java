package org.eclipse.keyple.example.remote.transport.wspolling.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class KeypleDtoPublishQueue<T> {

    private final BlockingQueue<T> q;
    private final String webClientId;
    private static final Logger logger = LoggerFactory.getLogger(KeypleDtoPublishQueue.class);


    public KeypleDtoPublishQueue(String webClientId){
        this.webClientId = webClientId;
        q = new LinkedBlockingQueue<T>(1);
    }

    public String getWebClientId(){
        return this.webClientId;
    }

    public void init(){
        if(!q.isEmpty()){
            try {
                T state = q.take();
                logger.error("Remove un-consumed element : " + state);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            logger.debug("Keyple Dto Queue is empty");

        }
    }

    public void publish(T state){
        logger.debug("Publish new keyple Dto : " + state);
        try {
            if(q.size()>0){
                logger.warn("Warning call init() before publishing");
            }
            q.put(state);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public T get(long timeout) throws InterruptedException{

        T element =  q.poll(timeout, TimeUnit.MILLISECONDS);
        return element;

    }


}

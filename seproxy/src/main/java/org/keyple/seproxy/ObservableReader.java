package org.keyple.seproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in
 * case of specific reader events, the SE Proxy implements the ‘Observer’ design
 * pattern. The ObservableReader object is optionally proposed by plugins for
 * readers able to notify events in case of IO Error, SE Insertion or removal.
 *
 * @author Ixxi
 */
public abstract class ObservableReader implements ProxyReader {

    // TODO: Resync the synchronization logic around here.
    // It can be thread safe or not, but it can't be a bit of both, i.e. if we synchronize in notifyObservers, we should
    // also do it in (at|de)tachObserver. The code as it is is pretty useless.


    /**
     * an array referencing the registered ReaderObserver of the Reader.
     */
    private List<ReaderObserver> readerObservers = new ArrayList<ReaderObserver>();

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * add a ReaderObserver to the list of registered ReaderObserver for the
     * selected ObservableReader.
     *
     * @param calledBack
     *            the called back
     */
    public final void attachObserver(ReaderObserver calledBack) {
        this.readerObservers.add(calledBack);
    }

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * remove a ReaderObserver from the list of registered ReaderObserver for
     * the selected ObservableReader.
     *
     * @param calledback
     *            the calledback
     */
    public final void detachObserver(ReaderObserver calledback) {
        this.readerObservers.remove(calledback);
    }

    /**
     * This method shall be called only from a SE Proxy plugin by a reader implementing ObservableReader
     * push a ReaderEvent of the selected ObservableReader to its registered ReaderObserver.
     *
     * @param event the event
     */
    public void notifyObservers(ReaderEvent event) {
        synchronized (readerObservers) {
            for (ReaderObserver observer : readerObservers) {
                observer.notify(event);
            }
        }
    }

}
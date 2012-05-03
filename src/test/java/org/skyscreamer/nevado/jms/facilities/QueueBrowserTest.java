package org.skyscreamer.nevado.jms.facilities;

import junit.framework.Assert;
import org.junit.Test;
import org.skyscreamer.nevado.jms.AbstractJMSTest;
import org.skyscreamer.nevado.jms.NevadoMessageConsumer;
import org.skyscreamer.nevado.jms.NevadoSession;
import org.skyscreamer.nevado.jms.message.NevadoMessage;
import org.skyscreamer.nevado.jms.util.RandomData;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.util.Enumeration;

/**
 * Test Nevado implementation of queue browser
 *
 * @author Carter Page <carter@skyscreamer.org>
 */
public class QueueBrowserTest extends AbstractJMSTest {
    @Test
    public void testQueueBrowser() throws JMSException, InterruptedException {
        NevadoSession session = createSession();
        TextMessage[] msgs = new TextMessage[5];
        for(int i = 0 ; i < 5 ; ++i) {
            msgs[i] = session.createTextMessage(RandomData.readString());
        }
        Queue testQueue = session.createTemporaryQueue();
        MessageProducer producer = session.createProducer(testQueue);
        for(int i = 0 ; i < 5 ; ++i) {
            producer.send(msgs[i]);
            Thread.sleep(200);
        }
        QueueBrowser browser = session.createBrowser(testQueue);
        Enumeration e = browser.getEnumeration();
        for(int i = 0 ; e.hasMoreElements() ; ++i )
        {
            NevadoMessage msg = (NevadoMessage)e.nextElement();
            Assert.assertEquals("Message " + i, msgs[i], msg);
            ++i;
        }
        NevadoMessageConsumer consumer = session.createConsumer(testQueue);
        for(int i = 0 ; i < 5 ; ++i) {
            NevadoMessage msg = consumer.receiveNoWait();
            Assert.assertEquals(msgs[i], msg);
        }
    }

    @Test
    public void testBrowseEmptyQueue() throws JMSException {
        NevadoSession session = createSession();
        Queue testQueue = session.createTemporaryQueue();
        QueueBrowser browser = session.createBrowser(testQueue);
        Assert.assertFalse(browser.getEnumeration().hasMoreElements());
    }

    @Test(expected = IllegalStateException.class)
    public void testAcknowledgeBrowsedMessage() throws JMSException {
        NevadoSession session = createSession();
        Queue testQueue = session.createTemporaryQueue();
        MessageProducer producer = session.createProducer(testQueue);
        producer.send(session.createMessage());
        QueueBrowser browser = session.createBrowser(testQueue);
        Assert.assertTrue(browser.getEnumeration().hasMoreElements());
        ((NevadoMessage)browser.getEnumeration().nextElement()).acknowledge();
    }
}

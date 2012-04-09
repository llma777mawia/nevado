package org.skyscreamer.nevado.jms.facilities;

import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.nevado.jms.AbstractJMSTest;
import org.skyscreamer.nevado.jms.util.RandomData;

import javax.jms.*;

/**
 * Tests starting and stopping connections (JMS 1.1, Sec. 4.3.3 & 4.3.4)
 *
 * @author Carter Page <carter@skyscreamer.org>
 */
public class ConnectionStopStartTest extends AbstractJMSTest {
    @Test
    public void testClientStart() throws Exception {
        clearTestQueue();
        Connection conn = createConnection(getConnectionFactory());
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(getTestQueue());
        String testBody = RandomData.readString();
        TextMessage testMessage = session.createTextMessage(testBody);
        producer.send(testMessage);

        MessageConsumer consumer = session.createConsumer(getTestQueue());
        Message msg = consumer.receive(500);
        Assert.assertNull(msg);
        conn.start();
        msg = consumer.receiveNoWait();
        msg.acknowledge();
        Assert.assertTrue(msg instanceof TextMessage);
        Assert.assertEquals(testBody, ((TextMessage)msg).getText());
    }
    
    @Test
    public void testClientPause() throws Exception {
        // Set up and send two messages
        clearTestQueue();
        Connection conn = getConnection();
        Session session = createSession();
        String testBody1 = RandomData.readString();
        String testBody2 = RandomData.readString();
        MessageProducer producer = session.createProducer(getTestQueue());
        producer.send(session.createTextMessage(testBody1));
        producer.send(session.createTextMessage(testBody2));

        // Wait for the first message
        MessageConsumer consumer = session.createConsumer(getTestQueue());
        Message msg = consumer.receive();
        msg.acknowledge();
        Assert.assertTrue(msg instanceof TextMessage);
        Assert.assertEquals(testBody1, ((TextMessage)msg).getText());

        // Pause and ensure the second message isn't coming
        conn.stop();
        msg = consumer.receive(500);
        Assert.assertNull(msg);

        // Restart and pick up second message
        conn.start();
        msg = consumer.receiveNoWait();
        msg.acknowledge();
        Assert.assertTrue(msg instanceof TextMessage);
        Assert.assertEquals(testBody2, ((TextMessage) msg).getText());
    }
}

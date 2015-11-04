import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Producer {

  public static void main(String[] args) throws Exception {

    if (args.length != 1){
      System.out.println("You need to specify the sending message.");
      System.exit(1);
    }

    try {
      // Create a ConnectionFactory
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.start();

      // Create a Session
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // Create the destination (Topic or Queue)
      Destination destination = session.createQueue("SAMPLE");

      // Create a MessageProducer from the Session to the Topic or Queue
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      // Create a messages
      String text = args[0];
      TextMessage message = session.createTextMessage(text);

      // Tell the producer to send the message
      producer.send(message);

      // Clean up
      session.close();
      connection.close();
    }
    catch (Exception e) {
       System.out.println("Caught: " + e);
       e.printStackTrace();
    }
  }
}
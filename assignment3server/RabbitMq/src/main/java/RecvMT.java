import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import io.swagger.client.model.LiftRide;
import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecvMT {

  // persistent
  private final static String QUEUE_NAME = "threadExQ";
  // non-persistent
//  private final static String QUEUE_NAME = "threadExQ-non";
  private static final int NUM_OF_THREAD = 20;
  private static final Logger logger = LogManager.getLogger(RecvMT.class);

  public static void main(String[] argv) throws Exception {
    LiftRideDao liftRideDAO = new LiftRideDao();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("username");
    factory.setPassword("password");
    factory.setVirtualHost("/");
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
        // non-persistent
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//         persistent
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

//        System.out.println("ok: " + ok);
        // max one message per receiver
        channel.basicQos(1);
        System.out.println(" [*] (" + NUM_OF_THREAD +
            ") Threads waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          try {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println(
                "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
                    + "'");
            String[] splitMessage = message.split("\n");
//            System.out.println(Arrays.toString(splitMessage));
            if (splitMessage.length != 5) {
//              System.out.println("Error! Invalid length of input message: " + message);
              logger.error("Trouble! message: " + message);
            } else {
              LiftRide newLiftRide = new LiftRide();
              newLiftRide.setResortID(splitMessage[0]);
              newLiftRide.setDayID(splitMessage[1]);
              newLiftRide.setSkierID(splitMessage[2]);
              newLiftRide.setTime(splitMessage[3]);
              newLiftRide.setLiftID(splitMessage[4]);
//              System.out.println("Got a message: " + Arrays.toString(splitMessage));
              liftRideDAO.createLiftRide(newLiftRide);
              logger.debug("message: (" + message + " ) got!");
            }
          } catch (Exception e) {
            logger.error("Trouble! ", e);
          }
        };

        // process messages
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
      } catch (IOException ex) {
        logger.log(Level.FATAL, ex);
      }
    };
    // start threads and block to receive messages
    for (int i = 0; i < NUM_OF_THREAD; i++) {
      Thread recv = new Thread(runnable);
      recv.start();
    }
//    Thread recv1 = new Thread(runnable);
//    Thread recv2 = new Thread(runnable);
//    Thread recv3 = new Thread(runnable);
//    recv1.start();
//    recv2.start();
//    recv3.start();
  }
}
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import com.rabbitmq.client.Channel;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {
  private ConnectionFactory factory;

  public ChannelFactory(ConnectionFactory factory) {
    this.factory = factory;
  }

  @Override
  public Channel create() throws Exception {
    Connection connection = factory.newConnection();
    return connection.createChannel();
  }

  /**
   * Use the default PooledObject implementation.
   */
  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }
}
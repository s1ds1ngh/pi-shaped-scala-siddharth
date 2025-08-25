import com.rabbitmq.client.{Connection, ConnectionFactory, Channel}
import java.util.concurrent.TimeUnit
import scala.util.{Try, Success, Failure}

object RabbitMQConfig {
  
  // Configuration constants
  val EXCHANGE_NAME = "order-exchange"
  val QUEUE_NAME = "order-queue"
  val ROUTING_KEY = "order.placed"
  
  // RabbitMQ connection parameters
  val HOST = "localhost"
  val PORT = 5672
  val USERNAME = "guest"
  val PASSWORD = "guest"
  
  private val connectionFactory: ConnectionFactory = {
    val factory = new ConnectionFactory()
    factory.setHost(HOST)
    factory.setPort(PORT)
    factory.setUsername(USERNAME)
    factory.setPassword(PASSWORD)
    factory.setConnectionTimeout(30000)
    factory.setRequestedHeartbeat(60)
    factory
  }
  
  def createConnection(): Try[Connection] = {
    Try(connectionFactory.newConnection())
  }
  
  def setupExchangeAndQueue(channel: Channel): Try[Unit] = {
    Try {
      // Declare exchange (topic type for flexible routing)
      channel.exchangeDeclare(EXCHANGE_NAME, "topic", true)
      
      // Declare queue (durable for persistence)
      channel.queueDeclare(QUEUE_NAME, true, false, false, null)
      
      // Bind queue to exchange with routing key
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY)
      
      println(s"Successfully set up exchange: $EXCHANGE_NAME and queue: $QUEUE_NAME")
    }
  }
  
  def withChannel[T](connection: Connection)(operation: Channel => T): Try[T] = {
    val channel = connection.createChannel()
    try {
      Success(operation(channel))
    } catch {
      case ex: Exception => Failure(ex)
    } finally {
      if (channel.isOpen) channel.close()
    }
  }
  
  def closeConnection(connection: Connection): Unit = {
    if (connection.isOpen) {
      connection.close()
    }
  }
}


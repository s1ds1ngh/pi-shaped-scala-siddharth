import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.rabbitmq.client.{Connection, Channel}
import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Try, Success, Failure, Random}

object OrderProducer {
  
  // Messages that the OrderProducer actor can receive
  sealed trait Command
  case class PlaceOrder(order: Order, replyTo: ActorRef[OrderPlacementResult]) extends Command
  case class GenerateRandomOrder(replyTo: ActorRef[OrderPlacementResult]) extends Command
  private case object Initialize extends Command
  
  // Response messages
  sealed trait OrderPlacementResult
  case class OrderPlacedSuccessfully(orderId: String, eventId: String) extends OrderPlacementResult
  case class OrderPlacementFailed(orderId: String, reason: String) extends OrderPlacementResult
  
  def apply(): Behavior[Command] = {
    Behaviors.setup { context =>
      new OrderProducer(context)
    }
  }
}

class OrderProducer(context: ActorContext[OrderProducer.Command]) 
    extends AbstractBehavior[OrderProducer.Command](context) {
  
  import OrderProducer._
  
  private var connection: Option[Connection] = None
  
  // Initialize RabbitMQ connection
  context.self ! Initialize
  
  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Initialize =>
        initializeRabbitMQ()
        this
        
      case PlaceOrder(order, replyTo) =>
        publishOrderPlacedEvent(order, replyTo)
        this
        
      case GenerateRandomOrder(replyTo) =>
        val randomOrder = generateRandomOrder()
        publishOrderPlacedEvent(randomOrder, replyTo)
        this
    }
  }
  
  private def initializeRabbitMQ(): Unit = {
    RabbitMQConfig.createConnection() match {
      case Success(conn) =>
        connection = Some(conn)
        
        // Setup exchange and queue
        RabbitMQConfig.withChannel(conn) { channel =>
          RabbitMQConfig.setupExchangeAndQueue(channel)
        } match {
          case Success(_) =>
            context.log.info("RabbitMQ connection and setup successful")
          case Failure(ex) =>
            context.log.error("Failed to setup RabbitMQ exchange and queue", ex)
        }
        
      case Failure(ex) =>
        context.log.error("Failed to connect to RabbitMQ", ex)
    }
  }
  
  private def publishOrderPlacedEvent(order: Order, replyTo: ActorRef[OrderPlacementResult]): Unit = {
    connection match {
      case Some(conn) =>
        val orderPlacedEvent = OrderPlaced(
          order.id,
          order,
          LocalDateTime.now(),
          UUID.randomUUID().toString
        )
        
        RabbitMQConfig.withChannel(conn) { channel =>
          val message = EventSerializer.serializeOrderPlaced(orderPlacedEvent)
          
          channel.basicPublish(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            null,
            message.getBytes("UTF-8")
          )
          
          context.log.info(s"Published OrderPlaced event for order ${order.id}")
          orderPlacedEvent
        } match {
          case Success(event) =>
            replyTo ! OrderPlacedSuccessfully(event.orderId, event.eventId)
          case Failure(ex) =>
            context.log.error(s"Failed to publish order event for ${order.id}", ex)
            replyTo ! OrderPlacementFailed(order.id, ex.getMessage)
        }
        
      case None =>
        context.log.error("RabbitMQ connection not available")
        replyTo ! OrderPlacementFailed(order.id, "RabbitMQ connection not available")
    }
  }
  
  private def generateRandomOrder(): Order = {
    val random = new Random()
    
    val customers = List(
      Customer("cust-001", "John Doe", "john.doe@email.com"),
      Customer("cust-002", "Jane Smith", "jane.smith@email.com"),
      Customer("cust-003", "Bob Johnson", "bob.johnson@email.com")
    )
    
    val products = List(
      Product("prod-001", "Laptop", BigDecimal("999.99"), "Electronics"),
      Product("prod-002", "Smartphone", BigDecimal("599.99"), "Electronics"),
      Product("prod-003", "Headphones", BigDecimal("199.99"), "Electronics"),
      Product("prod-004", "Book", BigDecimal("29.99"), "Books"),
      Product("prod-005", "Coffee Mug", BigDecimal("15.99"), "Home")
    )
    
    val customer = customers(random.nextInt(customers.length))
    val numItems = random.nextInt(3) + 1 // 1 to 3 items
    
    val orderItems = (1 to numItems).map { _ =>
      val product = products(random.nextInt(products.length))
      val quantity = random.nextInt(3) + 1 // 1 to 3 quantity
      OrderItem(product, quantity, product.price)
    }.toList
    
    Order(
      id = s"order-${UUID.randomUUID().toString.take(8)}",
      customer = customer,
      items = orderItems,
      orderDate = LocalDateTime.now(),
      status = OrderStatus.Pending
    )
  }
}


import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.rabbitmq.client.{Connection, Channel, Consumer, DefaultConsumer, Envelope, AMQP}
import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Try, Success, Failure, Random}
import scala.concurrent.Future
import scala.concurrent.duration._

object OrderConsumer {
  
  // Messages that the OrderConsumer actor can receive
  sealed trait Command
  case object StartConsuming extends Command
  case object StopConsuming extends Command
  case class ProcessOrderEvent(eventData: String) extends Command
  private case object Initialize extends Command
  
  def apply(): Behavior[Command] = {
    Behaviors.setup { context =>
      new OrderConsumer(context)
    }
  }
}

class OrderConsumer(context: ActorContext[OrderConsumer.Command]) 
    extends AbstractBehavior[OrderConsumer.Command](context) {
  
  import OrderConsumer._
  
  private var connection: Option[Connection] = None
  private var isConsuming = false
  
  // Initialize RabbitMQ connection
  context.self ! Initialize
  
  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Initialize =>
        initializeRabbitMQ()
        this
        
      case StartConsuming =>
        startConsuming()
        this
        
      case StopConsuming =>
        stopConsuming()
        this
        
      case ProcessOrderEvent(eventData) =>
        processOrderEvent(eventData)
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
            context.log.info("RabbitMQ connection and setup successful for consumer")
          case Failure(ex) =>
            context.log.error("Failed to setup RabbitMQ exchange and queue for consumer", ex)
        }
        
      case Failure(ex) =>
        context.log.error("Failed to connect to RabbitMQ for consumer", ex)
    }
  }
  
  private def startConsuming(): Unit = {
    if (isConsuming) {
      context.log.warn("Consumer is already consuming messages")
      return
    }
    
    connection match {
      case Some(conn) =>
        try {
          val channel = conn.createChannel()
          
          // Set QoS to process one message at a time
          channel.basicQos(1)
          
          val consumer = new DefaultConsumer(channel) {
            override def handleDelivery(
              consumerTag: String,
              envelope: Envelope,
              properties: AMQP.BasicProperties,
              body: Array[Byte]
            ): Unit = {
              val eventData = new String(body, "UTF-8")
              context.self ! ProcessOrderEvent(eventData)
              
              // Acknowledge the message
              channel.basicAck(envelope.getDeliveryTag, false)
            }
          }
          
          channel.basicConsume(RabbitMQConfig.QUEUE_NAME, false, consumer)
          isConsuming = true
          
          context.log.info("Started consuming messages from queue: " + RabbitMQConfig.QUEUE_NAME)
          
        } catch {
          case ex: Exception =>
            context.log.error("Failed to start consuming messages", ex)
        }
        
      case None =>
        context.log.error("RabbitMQ connection not available for consuming")
    }
  }
  
  private def stopConsuming(): Unit = {
    isConsuming = false
    context.log.info("Stopped consuming messages")
  }
  
  private def processOrderEvent(eventData: String): Unit = {
    context.log.info(s"Processing order event: $eventData")
    
    try {
      // Simulate order processing
      simulateOrderFulfillment(eventData)
      
    } catch {
      case ex: Exception =>
        context.log.error("Failed to process order event", ex)
    }
  }
  
  private def simulateOrderFulfillment(eventData: String): Unit = {
    // Parse basic order information from JSON (simplified parsing)
    val orderIdPattern = """"orderId":\s*"([^"]+)"""".r
    val customerNamePattern = """"name":\s*"([^"]+)"""".r
    val totalAmountPattern = """"totalAmount":\s*([0-9.]+)""".r
    
    val orderId = orderIdPattern.findFirstMatchIn(eventData).map(_.group(1)).getOrElse("unknown")
    val customerName = customerNamePattern.findFirstMatchIn(eventData).map(_.group(1)).getOrElse("unknown")
    val totalAmount = totalAmountPattern.findFirstMatchIn(eventData).map(_.group(1)).getOrElse("0.00")
    
    context.log.info(s"📦 Processing Order: $orderId")
    context.log.info(s"👤 Customer: $customerName")
    context.log.info(s"💰 Total Amount: $$${totalAmount}")
    
    // Simulate processing steps
    val processingSteps = List(
      "Validating order details",
      "Checking inventory availability",
      "Processing payment",
      "Preparing items for shipment",
      "Generating shipping label",
      "Order fulfillment completed"
    )
    
    processingSteps.zipWithIndex.foreach { case (step, index) =>
      Thread.sleep(500) // Simulate processing time
      context.log.info(s"  ${index + 1}. $step ✅")
    }
    
    // Generate fulfillment confirmation
    val fulfillmentId = s"fulfillment-${UUID.randomUUID().toString.take(8)}"
    val estimatedDelivery = LocalDateTime.now().plusDays(3 + Random.nextInt(5))
    
    context.log.info(s"🎉 Order $orderId successfully fulfilled!")
    context.log.info(s"📋 Fulfillment ID: $fulfillmentId")
    context.log.info(s"🚚 Estimated Delivery: $estimatedDelivery")
    
    // Simulate publishing OrderFulfilled event
    publishOrderFulfilledEvent(orderId, fulfillmentId)
    
    context.log.info("=" * 60)
  }
  
  private def publishOrderFulfilledEvent(orderId: String, fulfillmentId: String): Unit = {
    connection match {
      case Some(conn) =>
        val orderFulfilledEvent = OrderFulfilled(
          orderId,
          s"Fulfillment ID: $fulfillmentId, Status: Completed",
          LocalDateTime.now(),
          UUID.randomUUID().toString
        )
        
        RabbitMQConfig.withChannel(conn) { channel =>
          val message = EventSerializer.serializeOrderFulfilled(orderFulfilledEvent)
          
          // Publish to a different routing key for fulfilled orders
          channel.basicPublish(
            RabbitMQConfig.EXCHANGE_NAME,
            "order.fulfilled",
            null,
            message.getBytes("UTF-8")
          )
          
          context.log.info(s"📤 Published OrderFulfilled event for order $orderId")
        } match {
          case Success(_) =>
            // Event published successfully
          case Failure(ex) =>
            context.log.error(s"Failed to publish OrderFulfilled event for $orderId", ex)
        }
        
      case None =>
        context.log.error("RabbitMQ connection not available for publishing")
    }
  }
}


import akka.actor.typed.{ActorSystem, ActorRef}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import java.time.LocalDateTime

object ProducerApp extends App {
  
  implicit val system: ActorSystem[OrderProducer.Command] = ActorSystem(OrderProducer(), "order-producer-system")
  implicit val executionContext: ExecutionContext = system.executionContext
  implicit val timeout: Timeout = Timeout(10.seconds)
  
  val producer: ActorRef[OrderProducer.Command] = system
  
  println("=== Event-Driven Architecture E-commerce Order Producer ===")
  println("Starting order generation...")
  
  // Generate and publish multiple random orders
  val orderFutures = (1 to 5).map { i =>
    val future: Future[OrderProducer.OrderPlacementResult] = 
      producer.ask(OrderProducer.GenerateRandomOrder)
    
    future.onComplete {
      case Success(OrderProducer.OrderPlacedSuccessfully(orderId, eventId)) =>
        println(s"✅ Order $i: Successfully placed order $orderId with event $eventId")
      case Success(OrderProducer.OrderPlacementFailed(orderId, reason)) =>
        println(s"❌ Order $i: Failed to place order $orderId - $reason")
      case Failure(ex) =>
        println(s"❌ Order $i: Unexpected error - ${ex.getMessage}")
    }
    
    future
  }
  
  // Wait for all orders to complete
  Future.sequence(orderFutures).onComplete { _ =>
    println("\n=== Order generation completed ===")
    println("Check the consumer application to see order processing results.")
    
    // Keep the system running for a bit to ensure all messages are sent
    Thread.sleep(2000)
    system.terminate()
  }
}


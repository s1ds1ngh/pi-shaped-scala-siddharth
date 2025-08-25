import akka.actor.typed.{ActorSystem, ActorRef}
import scala.concurrent.duration._
import scala.io.StdIn

object ConsumerApp extends App {
  
  implicit val system: ActorSystem[OrderConsumer.Command] = ActorSystem(OrderConsumer(), "order-consumer-system")
  
  val consumer: ActorRef[OrderConsumer.Command] = system
  
  println("=== Event-Driven Architecture E-commerce Order Consumer ===")
  println("Starting order processing service...")
  println("Waiting for OrderPlaced events from RabbitMQ...")
  println("Press ENTER to stop the consumer")
  
  // Start consuming messages
  consumer ! OrderConsumer.StartConsuming
  
  // Wait for user input to stop
  StdIn.readLine()
  
  println("Stopping consumer...")
  consumer ! OrderConsumer.StopConsuming
  
  // Give some time for cleanup
  Thread.sleep(1000)
  system.terminate()
  
  println("Consumer stopped.")
}


import java.time.LocalDateTime
import java.util.UUID

// Domain models for the e-commerce system
case class Customer(
  id: String,
  name: String,
  email: String
)

case class Product(
  id: String,
  name: String,
  price: BigDecimal,
  category: String
)

case class OrderItem(
  product: Product,
  quantity: Int,
  unitPrice: BigDecimal
) {
  def totalPrice: BigDecimal = unitPrice * quantity
}

case class Order(
  id: String,
  customer: Customer,
  items: List[OrderItem],
  orderDate: LocalDateTime,
  status: OrderStatus
) {
  def totalAmount: BigDecimal = items.map(_.totalPrice).sum
}

// Order status enumeration
sealed trait OrderStatus
object OrderStatus {
  case object Pending extends OrderStatus
  case object Confirmed extends OrderStatus
  case object Processing extends OrderStatus
  case object Shipped extends OrderStatus
  case object Delivered extends OrderStatus
  case object Cancelled extends OrderStatus
}

// Event definitions for Event-Driven Architecture
sealed trait OrderEvent {
  def orderId: String
  def timestamp: LocalDateTime
  def eventId: String
}

case class OrderPlaced(
  orderId: String,
  order: Order,
  timestamp: LocalDateTime = LocalDateTime.now(),
  eventId: String = UUID.randomUUID().toString
) extends OrderEvent

case class OrderConfirmed(
  orderId: String,
  timestamp: LocalDateTime = LocalDateTime.now(),
  eventId: String = UUID.randomUUID().toString
) extends OrderEvent

case class OrderFulfilled(
  orderId: String,
  fulfillmentDetails: String,
  timestamp: LocalDateTime = LocalDateTime.now(),
  eventId: String = UUID.randomUUID().toString
) extends OrderEvent

// JSON serialization utilities
object EventSerializer {
  import scala.util.{Try, Success, Failure}
  
  def serializeOrderPlaced(event: OrderPlaced): String = {
    s"""{
      "eventType": "OrderPlaced",
      "eventId": "${event.eventId}",
      "orderId": "${event.orderId}",
      "timestamp": "${event.timestamp}",
      "order": {
        "id": "${event.order.id}",
        "customer": {
          "id": "${event.order.customer.id}",
          "name": "${event.order.customer.name}",
          "email": "${event.order.customer.email}"
        },
        "items": [${event.order.items.map(serializeOrderItem).mkString(",")}],
        "totalAmount": ${event.order.totalAmount},
        "status": "${event.order.status}"
      }
    }"""
  }
  
  private def serializeOrderItem(item: OrderItem): String = {
    s"""{
      "product": {
        "id": "${item.product.id}",
        "name": "${item.product.name}",
        "price": ${item.product.price},
        "category": "${item.product.category}"
      },
      "quantity": ${item.quantity},
      "unitPrice": ${item.unitPrice},
      "totalPrice": ${item.totalPrice}
    }"""
  }
  
  def serializeOrderFulfilled(event: OrderFulfilled): String = {
    s"""{
      "eventType": "OrderFulfilled",
      "eventId": "${event.eventId}",
      "orderId": "${event.orderId}",
      "timestamp": "${event.timestamp}",
      "fulfillmentDetails": "${event.fulfillmentDetails}"
    }"""
  }
}


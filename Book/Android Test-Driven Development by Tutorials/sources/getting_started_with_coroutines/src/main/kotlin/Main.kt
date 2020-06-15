import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Launching a coroutine
/*
fun main() {
  (1..10000).forEach {
    GlobalScope.launch {
      val threadName = Thread.currentThread().name
      println("$it printed on thread ${threadName}")
    }
  }
  Thread.sleep(1000)
}
*/

// Digging deeper into coroutines
/*
fun main() {
  GlobalScope.launch {
    println("Hello coroutine!")
    delay (500)
    println("Right back at ya!")
  }
  Thread.sleep(1000)
}
*/

// Dependent Jobs in action
fun main() {
  val job1 = GlobalScope.launch(start = CoroutineStart.LAZY) {
    delay(200)
    println("Pong")
    delay(200)
  }

  GlobalScope.launch {
    delay(200)
    println("Ping")
    job1.join()
    println("Ping")
    delay(200)
  }
  Thread.sleep(1000)
}
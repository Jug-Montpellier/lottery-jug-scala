package lottery

import com.typesafe.config.ConfigFactory

object LotteryConf {

  private val config = ConfigFactory.load()
  val token          = config.getString("eventbrite.token")
  val organizerId    = config.getString("eventbrite.organizer.id")
  val cacheTTL       = config.getInt("eventbrite.cache")
}

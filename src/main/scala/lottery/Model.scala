package lottery

case class Pagination(object_count: Int, page_number: Int, page_size: Int, page_count: Int)
case class Attendeed(name: Option[String], first_name: Option[String], last_name: Option[String])
case class Attendees(pagination: Pagination, attendees: List[Attendeed])

case class Event(id: String)
case class EventDescription(id: String, nbAttendee: Int)

case class EventPage(pagination: Pagination, events: List[Event])

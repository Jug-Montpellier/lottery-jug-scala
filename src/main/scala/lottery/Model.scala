package lottery

case class Pagination(object_count: Int, page_number: Int, page_size: Int, page_count: Int)
case class Attendeed(name: Option[String], first_name: Option[String], last_name: Option[String], email: Option[String] )
case class Attendees(pagination: Pagination, attendees: List[Attendeed])
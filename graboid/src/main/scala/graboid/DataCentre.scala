package graboid

/** @param id
  * @param event
  *   https://www.fdsn.org/webservices/fdsnws-event-1.2.pdf
  * @param dataselect
  *   https://www.fdsn.org/webservices/fdsnws-dataselect-1.1.pdf
  */
final case class DataCentre(
    id: String,
    event: Option[String],
    dataselect: Option[String]
)

package model

import org.joda.time.DateTime

trait Calendar {
  def now: DateTime
}

object DefaultCalendar extends Calendar {
  override def now: DateTime = DateTime.now()
}

package tremors.graboid

import java.net.URL

given Conversion[URL, String] = _.toString

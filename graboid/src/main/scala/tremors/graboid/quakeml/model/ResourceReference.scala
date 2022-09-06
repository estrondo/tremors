package tremors.graboid.quakeml.model

import java.net.URI

opaque type ResourceReference = URI

def newResourceReference(value: String | URI): ResourceReference = 
  value match
    case string: String => new URI(string)
    case uri: URI => uri

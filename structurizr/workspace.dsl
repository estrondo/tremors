workspace "Tremors" "Model of Tremors Earthquake Web Monitoring" {
  
  model {
    anonymousUser = person "Anynomous" "An unauthenticated person"

    seismoProvider = element "Seismo Provider" "FDSN Service" "It's normally a FDSN Event Service" { 
      tags = "ExternalSystem"
    }

    tremors = softwareSystem "Tremors" "System to present earthquakes information" {

      configServer = container "Config Server" "It provides all configuration for the all systems" "Node.js"

      database = container "Database" "" "ArangoDB" {
        tags = "Database, ArangoDB, SpatialDatabase"

        graboidDatabase = component "Graboid Database"
        notifierDatabase = component "Notifier Database"
        tophDatabase = component "Toph Database"
      }

      messageBroker = container "Message broker" "" "Apache Kafka" {
        tags = "MessageBroker, ApacheKafka"

        detectedSeismoTopic = component "detected-seismo" "Detected Seismo Topic" "protobuf"
        publishedSeismoTopic = component "published-seismo" "Published Seismo Topic" "protobuf"
      }

      graboid = container "Graboid" "It's responsible to collect all information about seismos events" "ZIO Application" {
          tags = "Robot"

          graboidCrawlerManager = component "Crawler Manager" "It's responsible to control lifecycle of Crawler Supervisors and Crawlers."
          graboidTimelineManager = component "Timeline Manager" "It's responsible to select the correct events and manage the timeline."
          graboidTimelineRepository = component "Timeline Repository" "It's responsible to store/retrieve/find time windows."


          group "Crawler" {
            graboidCrawler = component "Crawler" "It's responsible to find new events." "zio.ZStream / AltoXML"
            graboidCrawlerSupervisor = component "Crawler Supervisor" "It's responsible to manage timeline from an event information provider."
          }

          graboidCrawler -> seismoProvider "Requests all seismos for a specified period" "FDSN Query => QuakeML"
          graboidCrawlerManager -> graboidCrawler "Creates new ones"
          graboidCrawlerManager -> graboidCrawlerSupervisor "Controls lifecycle" "create, restart, stop"
          graboidCrawlerManager -> graboidDatabase "Uses"
          graboidCrawlerSupervisor -> graboidCrawler "Organises all seismos information received from the Crawler" "ZIO Stream"
          graboidCrawlerSupervisor -> graboidTimelineManager "Uses it to obtain all information"
          graboidCrawlerSupervisor -> detectedSeismoTopic "Publishes the all new seismos information"
          graboidCrawlerSupervisor -> graboidDatabase "Uses"
          graboidTimelineManager -> graboidTimelineRepository "Uses it"
          graboidTimelineRepository -> graboidDatabase "Uses it" "collection: crawlerTimeline"
      }
  
      toph = container "Toph" "It's responsible to centralize all seismos information." "ZIO, gRPC" {
        tophSeismoListener = component "Seismo Listener" "It listens to seismos.event"
        tophSeismoCataloguer = component "Seismo Cataloguer" "It organises all seismos"
        tophSeismoCatalogue = component "Seismo Catalogue" "Store all seismos events"

        tophSeismoListener -> detectedSeismoTopic "Listens to it"
        tophSeismoListener -> tophSeismoCataloguer "Invokes"
        tophSeismoCataloguer -> publishedSeismoTopic "Publishes to seismo.journal"
        tophSeismoCataloguer -> tophSeismoCatalogue "Uses"
        tophSeismoCatalogue -> tophDatabase "Uses"
      }

      webContext = container "Web Context" "Delivers WebApp's content"

      webApp = container "Web Application" "Vue.js" {
        tags = "WebBrowser"
      }


      notifier = container "Notifier" "It's responsible to notify registered users about seismos events" "cats-effect, gRPC" {
        tags = "Robot"

        notifierSeismoListener = component "Seismo Journal Listener" "It listens to new published seismos"
        notifierWhatsAppNotifier = component "WhatsApp Notifier" "It's responsible to notify WhatsApp Users"
        notifierTelegramNotifier = component "Telegram Notifier" "It's responsible to notify Telegram Users"
        notifierNotificationService = component "Notification Service" "It's responsible to send notifications to users"
        notifierUserRepository = component "User Repository"
        notifierUserService = component "User Service"

        notifierSeismoListener -> publishedSeismoTopic "Listen to it"
        notifierSeismoListener -> notifierNotificationService "Invokes"

        notifierNotificationService -> notifierUserRepository "Uses"
        notifierNotificationService -> notifierTelegramNotifier "Uses"
        notifierNotificationService -> notifierWhatsAppNotifier "Uses"

        notifierUserRepository -> notifierDatabase "Uses"
        notifierUserService -> notifierUserRepository "Uses"
      }

      webAPI1 = container "Web API 1.x" "It provides access for all information." "zio-http" {
        webAPI1NotificationService = component "Notification Service" "It is used to interact with internal notification service."
        webAPI1PublishedSeismoListener = component "Published Seismo Listener"
        webAPI1EventOrganiser = component "Event Organiser" "It's responsible to organise all seismo information"

        webAPI1NotificationService -> notifierNotificationService "Uses" "gRPC"
        webAPI1PublishedSeismoListener -> publishedSeismoTopic "Listens to"
        webAPI1PublishedSeismoListener -> webAPI1EventOrganiser "Invokes"
        webAPI1SpatialHandler = component "Spatial Handler" "It's responsible to respond to the WebMapping"
      }

      graboid -> configServer "Uses"
      notifier -> configServer "Uses"
      webAPI1 -> configServer "Uses"

      anonymousUser -> webApp "Uses"
      webContext -> webApp "Delivers"
      webApp -> webAPI1 "Requests" "http2/json"
    }

    deploymentEnvironment "production"  {

      deploymentGroup "digital-ocean" {

        deploymentNode "do-01" "docker" {
          containerInstance graboid
          containerInstance webAPI1
          containerInstance webContext
          containerInstance notifier
          containerInstance toph
        }

        deploymentNode "do-02" "docker" {
          containerInstance messageBroker
          containerInstance database
          containerInstance configServer
        }

        deploymentNode "User computer" "WebBrowser" {
          containerInstance webApp
        }
      }
    }

  views {

    systemContext tremors "context-tremors" "All systems" {
      include *
      autoLayout lr
    }

    container tremors "tremors" "All conteiners" {
      include *
      autoLayout rl
    }
    component graboid {
      include *
      autoLayout tb
    }

    component toph {
      include *
      autoLayout tb
    }

    component notifier {
      include *
      autoLayout lr
    }

    component database {
      include *
      autoLayout tb
    }

    component messageBroker "message-broker" "All topics" {
      include detectedSeismoTopic publishedSeismoTopic
      include graboidCrawlerSupervisor tophSeismoListener tophSeismoCataloguer notifierSeismoListener webAPI1PublishedSeismoListener
      exclude "tophSeismoListener -> tophSeismoCataloguer"
      autoLayout rl
    }

    styles {

      element "Element" {
        background #ffffff
        stroke #888888
        color #b8b8b8
        colour #404040
        border solid
        shape RoundedBox
      }

      element "Person" {
        shape Person
      }

      element "WebBrowser" {
        shape WebBrowser
      }

      element "ExternalSystem" {
        background #e6e6e6
      }

      element "MessageBroker" {
        shape Pipe
      }

      element "ApacheKafka" {
        icon data:image/png;base64,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
      }

      element "Robot" {
        shape Robot
      }

      element "ArangoDB" {
        icon data:image/png;base64,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
      }

      element "Database" {
        shape Cylinder
      }
    }
  }
}
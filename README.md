# Build Publisher Plugin

Jenkins plugin to publish notifications for all completed builds to a RabbitMQ instance.

It is configured on the main Jenkins "Configure System" page and listens to all jobs controlled by this host
(which can run locally or on remote nodes).
Once a job completes, it puts a message to the queue that is processed in a separate thread. 
This way processing and publishing (which can be slow, depending on the target system) does not block or slow down
the Jenkins server.

If the queue gets full, the plugin will write a message to log and discard the build notification.  

Sample notification message:

    {
      "jenkins_url": "http://localhost:8080/",
      "job_name": "check-coverage",
      "build_number": 61,
      "result": "FAILURE",
      "id": "61",
      "test_results": {
        "total_count": 7,
        "skip_count": 1,
        "fail_count": 2,
        "failed_tests": [
            "com.company.Class2Test.anotherBrokenTest",
            "com.company.Class2Test.thisWillBeBroken"
        ]
      }
    }

## How to install the plugin

At this moment the plugin is not available in Jenkins Plugins repository.
You can build it from source (see [Building the plugin from source code]() below). I will publish a release version to
 Releases section of this repository soon.

## Plugin requirements.

* Jenkins core version: 1.580.3+
* Java 7

## RabbitMQ configuration.

You need to provide:

* RabbitMQ server name (or IP address)
* RabbitMQ server port number for notifications (5672 in default RabbitMQ configuration)
* User Name - note that this user must be authorized to publish to the Exchange (see below)
* User Password. Will be stored on disk in encrypted format.
* Exchange Name. This is similar to "queue", but one level higher: once you publish message to an "Exchange",
it will be routed by RabbitMQ to proper queue(s). This field must contain an Exchange name that already exist
on the RabbitMQ server.
See http://www.rabbitmq.com/tutorials/tutorial-three-java.html about exchanges.


## Logging.

You can create a separate Log recorder in your Jenkins (menu "Manage Jenkins" -> "System Log" -> "Add new log recorder"). 
Name it "Build Publisher Plugin" or whatever you want, then click "Add" to add a new Logger with this pattern:

    org.jenkinsci.plugins.bpp  

## Building the plugin from source code

You need to have Gradle 2.5+ to build the plugin. Clone this repository and run in the project folder:
 
    gradle jpi -x test
   
This will create build/libs/jenkins-publisher.hpi file, which you can upload from your computer on the Advanced tab of Manage Plugins Jenkins page.
"-x test" option tells Gradle to skip tests.



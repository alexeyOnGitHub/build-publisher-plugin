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

## Logging.

You can create a separate Log recorder in your Jenkins (menu "Manage Jenkins" -> "System Log" -> "Add new log recorder"). 
Name it "Build Publisher Plugin" or whatever you want, then click "Add" to add a new Logger with this pattern:

    org.jenkinsci.plugins.bpp  



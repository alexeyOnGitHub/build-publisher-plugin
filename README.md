# Jenkins Publisher Plugin

Publish notifications for all completed builds to a RabbitMQ instance.

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

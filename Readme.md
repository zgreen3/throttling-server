# One of home-take-tasks during my recent (in the Y2022) job search:

Write a string-boot application that will contain 1 controller with one method that returns HTTP 200 and an empty body.
Write a functionality that will limit the number of requests from one IP address to this method in the amount of N pieces in X minutes.
If the number of requests is greater, then the error code 502 should be returned until the number of requests for the specified interval becomes lower than N.
It should be possible to configure these two parameters via the configuration file
Make it so that this restriction can be applied quickly to new methods and not only to controllers, but also to methods of classes of the service layer.
The implementation should take into account a multithreaded highly loaded execution environment and consume as few resources as possible (!important).
Also write a simple JUnit test that will emulate the operation of parallel requests from different IP addresses.

!!! Do not use third-party libraries for trottling.List of technologies and tools:
The code should be described in Java 11 (or higher)
Frameworks: Spring + Spring Boot
To build, use Gradle
Other auxiliary libraries are possible.
Write a JUnit test using JUnit 5.x (Junit Jupiter)
Write a simple dockerfile to wrap this application in docker

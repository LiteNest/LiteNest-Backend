# Introduction
### What is Container Cloud Desktop
Container Cloud Desktop is an innovative virtualization technology that allows users to access and manage their desktop environment through a cloud platform. No matter where they are, as long as they have an Internet connection, users can securely connect to their personal or work desktop from any device for a consistent usage experience. Container cloud desktops support multiple operating systems, are easy to scale, and offer powerful data protection capabilities, making them ideal for remote working and digital transformation.
The Container cloud desktop system takes full advantage of container virtualization technology to provide efficient and scalable desktop solutions through lightweight container instances. Containers have significant advantages over traditional virtual machines in terms of startup time, resource consumption, and system isolation. Container cloud Desktop leverages these benefits to provide users with a responsive, easy-to-manage desktop environment. In addition, the administrator can directly manage resources such as cloud desktop instances, volume instances, network instances, and images of users, greatly simplifying desktop maintenance and management.
Features of this application
### This application has the following features
* Make full use of Docker container virtualization technology: Make full use of Docker container virtualization technology to provide tenants with a stable and reliable cloud desktop environment. Docker's flexibility and lightweight nature allows us to quickly deploy, scale, and manage users' desktop environments while ensuring high application availability and performance stability.
* Plug-in system support: In order to meet the needs of different system administrators, we provide a flexible plug-in system. System administrators can develop and integrate various plug-ins according to their needs to extend the functions of the container cloud desktop platform. This allows system administrators to customize and optimize the cloud desktop platform according to their own workflow and business needs, making their cloud desktop platform more flexible and personalized.
* Efficient design based on Spring Boot framework: This application is designed using Java's Spring Boot framework to ensure its efficiency and reliability. Spring Boot framework simplifies the development process and improves the response speed and performance of the system. With the support of Spring Boot, we can provide users with stable and efficient container cloud desktop services to meet their various work and application needs.
# Install, Experience and Contribute
### Install 
#### Install from source
1. Clone the Git Repository:
 ```shell
git clone https://github.com/ForestRealms/Container-Desktop-Backend.git
 ```
2. Change to project directory and build with [Maven](https://maven.apache.org/):
 ```shell
cd container-desktop-backend
mvn clean package -Dmaven.test.skip=true
 ```
3. Copy the application file, and save it properly:
```shell
cp ./target/*.jar /path/to/backend
```
#### Install form Release
Go to the [Release](https://github.com/ForestRealms/Container-Desktop-Backend/releases) page, 
select the appropriate version, and download the `*.jar` file inside, which is the application package

### Start Application
Make sure your JRE version is at least 21, and execute the following command to start the application:
```shell
java -Xmx4G -jar ContainerDesktopBackend-X.X.X.jar -Dspring.config.location=/path/to/application.yml
```
where:

* `-Xmx4G` means that the maximum amount of running memory allowed for the application itself is 4GB. If you want to adjust the maximum memory usage, change it. If you want the maximum running memory usage to be 2GB, replace it with -Xmx2G.
* `ContainerDesktopBackend-x.x.x.jar` indicates the package path or name. Replace it with a specific path or name.
* `-Dspring.config.location=/path/to/application.yml` indicates the location of the custom configuration file. Replace it with a specific location

### Configuration
The file `application.yml` above is called **Configuation File**, specifically, the default content is:
```yaml
spring:
  web:
    resources:
      static-locations:
        - classpath:/public/
  config:
    import:
      - classpath:BeanConfiguration.yml
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: "jdbc:mysql://192.168.117.20:3306/cloud?autoReconnect=true"
    username: root
    password: "123456"
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    open-in-view: false
  mvc:
    static-path-pattern: /**
jwt:
  secret-key: "A259724DFB5DDDB6C76A04C033890B970FFB3435FEFD5C08"
  validity-period: 7
  validity-unit: "DAY"
  token-header: "Authorization"
  token-head: "Bearer "

cors:
  allowed-origins: "*"
  allowed-methods: "*"
  allowed-headers: "*"
  allowed-credentials: false

container:
  host: "unix:///var/run/docker.sock"
  registry-username: ""
  registry-password: ""
  registry-email: ""
  registry-url: ""
  max-connections: 100
  connection-timeout: 30
  connection-timeout-unit: SECOND
  response-timeout: 45
  response-timeout-unit: SECOND
  auto-flush: true
  middle-image: "ubuntu:latest"
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%clr(%level)] %clr(%-15.15logger){cyan}：%msg%n"
  level:
    root: info
```
where:
* The `spring.datasource` section shows the connection Settings for the backend database of the application, specifically:
  * The default url is the MySQL database. The format of the URL is the default configuration file. `192.168.117.20:3306` indicates the IP address and port number of the database host, and cloud indicates the database name. `autoReconnect=true` indicates automatic reconnect. Please do not change other parts if there is no special requirement
  * `username` specifies the username of the database. It can be passed as an environment variable
  * `password` indicates the password of the database. It can be passed in the form of environment variables
* The `jwt` section indicates the Settings of the JWT (Java Web Token) used for token validation, specifically:
  * `secret-key` specifies the key. The length of the key **must be 256 characters or more**, for security purpose, `secret-key` **must be changed** in production environment
  * `validity-period` specifies the validity period of the token given to the user after the authentication request is successful
  * `validity-unit` indicates the unit of `validity-period`. The value can be `SECOND`, `MINUTE`, `HOUR`, or `DAY`, indicating second, minute, hour, or day respectively. The default value is day
  * `token-header` specifies the request header name used to carry the request header information of the token. The default value is `Authorization`
  * `token-head` indicates the value at the beginning of the string containing the content of the token request header. If the name of the request header is the value specified in the token-header, but its content does not start with the value specified in the token-head, the authentication information is invalid
* The `cors` section represents the backend cross-origin configuation, specifically:
  * `allowed-origins` specifies the allowed front end domain name. The string list is received. The value is `*` by default
  * `allowed-methods` specifies the allowed HTTP methods. The string list is received. The value is `*` by deafult
  * `allowed-headers` indicates a list of characters that are allowed to receive requests. The value is `*` by default
  * `forwards-credentials` specifies whether credentials are allowed and accepts Boolean values. The default value is `false`
* `container` represents the setting for the application to communicate with the container engine, specifically:
  * `host` indicates the host address of the container engine and the location of the socket file
  * `registry-username` indicates the username used when it comes to having the container engine pull images from the repository (especially private repositories)
  * `registry-password` Indicates the password used when it comes to having the container engine pull images from the repository (especially private repositories)
  * `registry-email` indicates the email address used when it comes to having container engines pull images from repositories (especially private repositories)
  * `registry-url` indicates the URL address used when it comes to having container engines pull images from repositories (especially private repositories)
  * `max-connections` indicates the maximum number of concurrent connections that the application can use to communicate with the container engine
  * `connection-timeout` specifies how long a connection is initiated. If no result is obtained, the connection is considered a timeout
  * `connection-timeout-unit` indicates the unit of the value specified by `connection-timeout`. The value can be `SECOND`, `MINUTE`, `HOUR`, or `DAY`, indicating second, minute, hour, or day respectively. The default value is second
  * `response-timeout` specifies how long it takes to initiate a request without receiving a response. If no response is received, the request is considered a timeout
  * `response-timeout-unit` specifies the unit of the value specified by `response-timeout`. It can be named `SECOND`, `MINUTE`, `HOUR`, or `DAY`, which are seconds, minutes, hours, or days respectively. The default value is DAY
  * `auto-flush` specifies whether the back-end database is automatically flushed upon application startup to synchronize changes stored in the container engine. This parameter receives a Boolean value and defaults to true
  * `middle-image` indicates an intermediate image. When the volume of a container is being expanded, if the underlying volume drive does not support direct expansion and data needs to be transferred from an intermediate container, the name or ID of the container image used is a character string. The default value is ubuntu:latest
* `logging` represents logging related Settings for back-end applications, specifically:
  * `pattern.console` indicates the log output style or format of the console
  * `level.root` indicates the lowest level for recording logs. It receives the following enumerated characters:
    * `trace`: Displays all the information and traces all the internal state of the application
    * `debug`: Displays debugging information. It is used for running in debugging state or troubleshooting
    * `info`: This is the default log level and shows the content of the necessary report
    * `warn`: Warning level that is generated when non-critical errors occur in the application
    * `error`: Error level, including when the application encounters an exception thrown, or when a serious error or crash is reported

### Contribute
Welcome to contribute to this project! Here are several ways you can contribute:
* Report Bugs: If you find any bugs, please submit a bug report on [GitHub Issues](https://github.com/ForestRealms/Container-Desktop-Backend/issues), describing the steps to reproduce the bug and the environment information.
* Provide Suggestions: If you have any suggestions or ideas to improve the project, please feel free to raise them on [GitHub Issues](https://github.com/ForestRealms/Container-Desktop-Backend/issues). We're always eager to hear your feedback.
* Write Documentation: Help improve the project's documentation, including the README, user manuals, etc.
* Fix Issues: If you have the ability to fix existing issues, please submit a Pull Request at any time. We'll review and merge your contributions as soon as possible.

### Contributors
<a href="https://github.com/LiteNest/LiteNest-API/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=LiteNest/LiteNest-API" />
</a>

### Committing Code

1. Fork this project to your GitHub account.
2. Clone the forked repository to your local environment: `git clone https://github.com/ForestRealms/Container-Desktop-Backend.git`
3. Create and switch to a new branch: `git checkout -b feature-name`
4. Write your code and perform the necessary tests.
5. Commit your changes: `git commit -am "Add feature"`
6. Push your changes to the remote branch: `git push origin feature-name`
7. Create a new Pull Request on GitHub, describing your changes and their purpose.
8. Wait for our review, and we'll provide feedback and process your Pull Request as soon as possible.

Thank you for your support and contributions to this project!

### Status
![Alt](https://repobeats.axiom.co/api/embed/8f58681e5ec797c9bd1a6efbee7886286c3253b6.svg "Repobeats analytics image")

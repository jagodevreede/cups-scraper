CUPS Scraper
============

A tiny Java utility that scrapes the CUPS web interface (jobs page) and exposes a simple HTTP endpoint returning the
number of print jobs in the queue as JSON. 

This project includes a small embedded HTTP server (com.sun.net.httpserver) and a simple HTML parser that counts table
row entries on the printer jobs page.

This project has been tested with CUPS 2.4.10

Prerequisites
-------------

- Java 21 JDK
- Maven 3.8+ (or your preferred Maven installation)

Build
-----
From the repository root:

```bash
mvn package
```

This produces a jar in target/ (e.g. target/cups-scraper-1.0-SNAPSHOT.jar).

Run
---
Run the packaged jar:

```bash
java -jar target/cups-scraper-1.0-SNAPSHOT.jar
```

The server listens on port 8080 by default. Visit:

- Root page: http://localhost:8080/
- JSON counter: http://localhost:8080/counter

The /counter endpoint will make an HTTPS request to the CUPS jobs page, parse the HTML, and return JSON like:

{ "count": 3 }

Command-line arguments
----------------------
This application accepts two optional command-line arguments which can be used when starting the packaged JAR or running
from your IDE:

- `--port <port>`
    - Sets the HTTP server port. Default: `8080`.
    - Example: `java -jar target/cups-scraper-1.0-SNAPSHOT.jar --port 9090`

- `--url <url>`
    - Sets the CUPS jobs page URL to scrape. Default: `https://printer.local:631/jobs/`.
    - Example: `java -jar target/cups-scraper-1.0-SNAPSHOT.jar --url https://my-printer:631/jobs/`

Notes about the CLI parsing

- The program expects argument pairs (`--port 9090 --url <url>`). If the arguments are malformed the application will
  print usage and exit with a non-zero status.
- The defaults are used when options are omitted.

Notes
----------------------

- Important security note: The HTML fetch implementation in `CupsQueueParser` disables SSL hostname verification and
  trusts all certificates. This is insecure and intended only for shortcuts in trusted networks or testing. Do not use
  this in production without replacing the trust manager with proper certificate validation.

Running tests
-------------
The project contains a small unit test for the parser. Run tests with:

```bash
mvn test
```

Building a native image
-----------------------
The project includes an optional Maven profile `native` that configures the GraalVM native image plugin. This requires
a GraalVM SDK to work

```bash
mvn -Pnative package
```

Running as service
------------------

The included `cups-scraper.service` file in the repository is a simple example but contains hard-coded paths and a fixed
user (`pi`) that you should adjust for your system before installing.

- Review and update `WorkingDirectory` to the absolute directory where you will place the application (for example
  `/home/<youruser>/cups-scraper/target/`).
- Update `ExecStart` to the absolute path of the runnable you intend to use. The example uses native image
  `/home/pi/cups-scraper/target/cups-scraper`; if you prefer to run the JAR directly, set it to something like
  `ExecStart=/usr/bin/java -jar /opt/cups-scraper/cups-scraper-1.0-SNAPSHOT.jar`. But ensure that java in the path or
  fully qualified.
- Change `User` and `Group` from `pi` to the system user that should own/run the service on your machine.

After making those adjustments you can copy the unit file to `/etc/systemd/system/`, reload systemd and enable the
service (for example: `sudo systemctl daemon-reload && sudo systemctl enable --now cups-scraper.service`).

License
-------
This project is released into the public domain under The Unlicense. See the included `LICENSE` file for the full text.

Contact / Support
-----------------
This project is not under active development. You are welcome to fork the repository, file issues, or submit pull
requests; contributions will be reviewed when time permits.

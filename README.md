# Scan Queue

A console app that queues dummy "scans" and runs them one at a time on a background thread. Commands are accepted at any time, including while a scan is running.

Requires Java 14 or higher to build.

## Build and run

    javac -d out src/*.java
    java -cp out Main

To build and run a JAR instead:

    jar cfm ScanQueue.jar manifest.txt -C out .
    java -jar ScanQueue.jar

The `ScanQueue.jar` in the repository was built with Java 21 and needs Java 21 or higher. Rebuild it as above to use an older version.

## Commands

Command words are not case-sensitive. Duration is in seconds. If pause is `Yes`, the queue stops after that scan completes until `start` is entered again.

| Command | Example | Description |
|---|---|---|
| `add:<id>, <name>, <duration>, <pause>` | `add:1, Scan A, 5, Yes` | Queue a scan |
| `view` | `view` | List all scans and their states |
| `start` | `start` | Run queued scans in order |
| `stop` | `stop` | Cancel the running scan; the queue continues |
| `remove:<id>` | `remove:3` | Remove a scan that has not started |
| `exit` | `exit` | Quit |

## Example

Lines starting with `>` are typed by the user.

    > add:1, Scan A, 5, No
    Added scan 1.
    > add:2, Scan B, 2, Yes
    Added scan 2.
    > start
    Starting Scan A
    > view
    Scan:1, Scan A, 5, No [RUNNING]
    Scan:2, Scan B, 2, Yes [IDLE]
    > stop
    Cancelled Scan A
    Starting Scan B
    Completed Scan B
    > view
    Scan:1, Scan A, 5, No [CANCELLED]
    Scan:2, Scan B, 2, Yes [COMPLETE]

## Docker

The Dockerfile builds the JAR and serves the console in a browser using [ttyd](https://github.com/tsl0922/ttyd). Each browser connection gets its own queue, and at most 5 can be connected at once.

    docker build -t scanqueue .
    docker run -p 10000:10000 -e TTYD_USER=user -e TTYD_PASS=pass scanqueue

Open `http://localhost:10000` and sign in with the username and password given above. The port can be changed with the `PORT` environment variable.

## Test login

Live version: https://task-queue-431h.onrender.com

It asks for a login. Use these credentials:

- Username: `manya`
- Password: `manya324`

## Notes

- Scan names cannot contain commas.
- Ids must be unique.
- A scan added after the queue has finished stays `IDLE` until the next `start`.
- `exit` cancels any running scan.
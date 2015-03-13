# AirVantage scheduling micro service

## Work In Progress

- There is no UI

## Configuration

The AVSCHED_CONF_DIR environment property should be set to the location of a configuration folder.
It should contain a "deploy-sched-local.properties" file.
A template exist in the "conf" folder.

### Mandatory

- `av-sched.secret` : secret string exchanged in all API calls. (See [Security](#security) )

- `av-sched.db.server`
- `av-sched.db.port`
- `av-sched.db.dbName`
- `av-sched.db.user`
- `av-sched.db.password`

Database credentials. You should create the db and user yourself.

### Optional

- `av-sched.port` : 8086 by default.

## Usage

### Build UI

- `npm install -g gulp`
- `npm install -g bower`
- `bower install`
- `gulp build` (or `gulp dev` if you want js files to be recompiled on the fly)

### Run server

#### From eclipse

- `mvn eclipse:eclipse`
- setup AVSCHED_CONF_DIR variable in build conf
- Run `SchedMain`

#### From jar

- `mvn package`
- `export AVSCHED_CONF_DIR=/home/....`
- `java -jar target/av-sched-x.y.z-exec.jar`

## API

## Security

All api calls to `av-sched` should contain the header `X-Sched-secret`, with the value of
the `av-sched.secret` parameter.

The application that implements the task should also check that this header has the same value (and return a 401 otherwise.)

Obviously, this secret should remain, *ahem*, [secret](http://uncyclopedia.wikia.com/wiki/Captain_Obvious).

### Schedule a job

~~~
POST host:8086/sched/api/job
{
  "id" : "av-server/timers",
  "url" : "http://murphy:3000/echo",
  "scheduling" : {
    "type" : "cron",
    "value" : "0/30 0/1 * 1/1 * ? *",
    "timeout" : 720000
  }
}
~~~

### Ack a job

~~~
POST host:8086/sched/api/job
{
  "id" : "av-server/timers"
}
~~~

## Functionnal Tests

See src/node/README.md

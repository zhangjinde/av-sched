# AirVantage scheduling micro service

## Work In Progress

- This uses an in-memory quartz scheduler and job store
- There is no UI
- There is no jar launcher

## Configuration

The AVSCHED_CONF_DIR environment property should be set to the location of a configuration folder.
It should contain a "deploy-sched-local.properties" file.
A template exist in the "conf" folder.

### Mandatory

- `av-sched.secret` : secret string exchanged in all API calls. (See [Security](#security) )

### Optional

- `av-sched.port` : 8086 by default.

### Security

All api calls to `av-sched` should contain the header `X-Sched-secret`, with the value of
the `av-sched.secret` parameter.

The application that implements the task should also check that this header has the same value (and return a 401 otherwise.)

Obviously, this secret should remain, *ahem*, [secret](http://uncyclopedia.wikia.com/wiki/Captain_Obvious).

## Usage

### From eclipse

- `mvn eclipse:eclipse`
- setup AVSCHED_CONF_DIR variable in build conf
- Run `SchedMain`

## API

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

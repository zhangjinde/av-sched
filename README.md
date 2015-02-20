AirVantage scheduling micro service
-----------------------------------

## Work In Progress

- This uses an in-memory quartz scheduler and job store
- There is no security
- There is no UI

## Usage

Run MainLauncher.java as an application.
This starts the server on port 8086.

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

# effectie2-examples
Example projects for Effectie v2

## Run

### Start the app
To run, open the sbt console
```sbt
project effectie2-examples-cats-effect3
```
to set the current project to the example project for Scala 2, then run
```sbt
reStart
```
The result might look like 
```sbt
[info] compiling 1 Scala source to /path/to/effectie2-examples/examples/effectie2-examples-cats-effect3/target/scala-2.13/classes ...
[info] Application effectie2-examples-cats-effect3 not yet started
[info] Starting application effectie2-examples-cats-effect3 in the background ...
effectie2-examples-cats-effect3 Starting example.MainApp.main()
[success] Total time: 1 s, completed 9 May 2022, 10:01:08 pm
effectie2-examples-cats-effect3 [io-compute-15] INFO  o.h.e.s.EmberServerBuilderCompanionPlatform - Ember-Server service bound to address: [::]:8080
```
Once you see it, it's ready for testing.

### Stop the app
```sbt
reStop
```
```sbt
[info] Stopping application effectie2-examples-cats-effect3 (by killing the forked JVM) ...
effectie2-examples-cats-effect3 ... finished with exit code 143
[success] Total time: 1 s, completed 9 May 2022, 10:17:07 pm
```


### Hello

```
http://localhost:8080/hello/World
```
```json
{
  "message": "Hello, World"
}
```


### Greet
Send a request with `curl`
```shell
curl --request POST \
  --url http://localhost:8080/greet \
  --header 'Content-Type: application/json' \
  --data '{
    "greet": "Hey",
    "to": "Kevin"
  }'
```
Result:
```json
{"message":"Hey Kevin"}
```
***
Send a request with [httpie](https://httpie.io)
```shell
http POST \
  http://localhost:8080/greet \
  greet=Hey \
  to=Kevin
```
Result:
```shell
HTTP/1.1 200 OK
Connection: keep-alive
Content-Length: 23
Content-Type: application/json
Date: Mon, 09 May 2022 11:53:24 GMT

{
    "message": "Hey Kevin"
}
```


### Joke
```
http://localhost:8080/joke
```
Then result might be like
```json
{
  "joke": "R.I.P. boiled water. You will be mist."
}
```

### Timeout Demo
* Timeout - http client used by `Jokes`
```
http://localhost:8080/test-client-timeout
```
```json
{
  "message": "[Client] The request timed out"
}
```

* Timeout - from the endpoint of app itself.
```
http://localhost:8080/test-server-timeout
```
```json
{
  "message": "Response timed out after 5 seconds"
}
```
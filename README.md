How it works:

```
[application.conf] ➔ ( UrlReader ) ➔ [ Seed URLs ]
                                            │
                                            ▼
                                  ( UrlProducer Pool )
                                            │  (Fetches HTML concurrently)
                                            ▼
                                  ┌──────────────────┐
                                  │ Bounded Queue    │
                                  └──────────────────┘
                                            │
                                            ▼
                                  ( HtmlConsumer Pool )
                                            │  (Parses links & outputs)
                                            ▼
                                  [ Console Output / Terminal ]

```

### 1. Read Seed URLs

`ConfigUrlReader` loads the initial list of target websites from `application.conf`.

### 2. Produce & Fetch (Producer)

`UrlProducer` takes the URLs and fetches their raw HTML pages concurrently. As each page is fetched, it pushes it into a
shared bounded queue.

### 3. Queue & Backpressure

The bounded queue sits between fetching and processing. If consumers fall behind, the queue safely pauses the producer
to prevent running out of memory.

### 4. Consume & Parse (Consumer)

`HtmlConsumer` workers continuously pull HTML pages from the queue, extract all hyperlinks using `JsoupHtmlParser`, and
print the results to the console.

### 5. Graceful Shutdown

Once the producer finishes fetching all pages, it sends completion signals through the queue so all consumers drain any
remaining items and shut down cleanly.

## Execute Application

To run the crawler with the configured URLs in `application.conf`:

```shell
sbt run
```

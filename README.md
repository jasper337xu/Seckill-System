# Description
This project implements an online shopping system that is able to handle a massive amount of traffic and high concurrency. The system guarantees data consistency, reliability and high availability. Use cases include Black Friday, Boxing Day, Cyber Monday, etc. during which an online shopping system needs to handle much more requests than ususal.

# Design Diagram
![alt text](https://github.com/jasper337xu/Seckill-System/blob/master/doc/image/Design_Diagram.jpg?raw=true)

# Key Points
- ## Massive Amount of Requests
  ### 1. Asynchronous Processing with Message Queue
  When a user submits an order, we would like to write the newly-created order to database. However, database is not able to handle a large number of writes at the same time (can only handle approx. 1000 QPS). Hence, when a large number of orders are submitted by users at the same time, writing to database directly would cause database to crash. 
  
  Message queue enables asynchronous processing. Message queue can be considered as a funnel in the sense that messages are consumed by consumer at a steady rate even if lots of messages are sent to message queue. Hence, all messages corresponding to newly-created orders are sent to message queue, and writing to database is handled in consumer asynchronously. This prevents database from crashing due to massive amount of requests.
  
  ### 2. Cache Warm-up
  When users browse commodity information or promotional activity information, controllers would need to read from database if we did not warm up cache. As mentioned above, database is not able to handle a large number of queries at the same time. Hence, database would probably crash if too many requests are made by users at the same time.

  This issue can be resolved by warming up cache. That is, we can preload into Redis cache promotional activity information and commodity information. With Redis cache, when lots of requests are sent to the system, the system will read from cache (i.e. cache hit) instead of database. Redis cache can handle approx. 100k QPS, which is much more powerful than database. Reading from cache is much faster than reading from database is because cache stores data in memory while database stores data on disk. Hence, cache warm-up greatly improves the system performance so that the system is capable of handling a massive amount of requests.

# Description
This project implements an online shopping system that can handle a massive amount of traffic and high concurrency. The system guarantees data consistency, reliability and high availability. Use cases include Black Friday, Boxing Day, Cyber Monday, etc. during which an online shopping system needs to handle much more requests than usual.

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

- ## Data Consistency
  Data consistency among multiple services (order service, payment service and stock service) is guaranteed by the system using distributed transaction. The core idea is that lock the stock when an order is created, and deduct the stock if payment is completed within limited time or revert the stock otherwise.
  ### Workflow
    **1. Create Order**<br/>
     Create an order when a user places an order. Lock the stock. Also send to message queue a delay message that checks whether payment is completed within limited time and handles closing timeout order. <br/>
    **2. Check Out**<br/>
     Call payment API to process the payment. <br/>
    **3. Send a Message of Payment Done**<br/>
     Send a message to message queue indicating that payment has been completed within limited time. <br/>
    **4. Consume a Message of Payment Done**<br/>
     Deduct the stock. Update the order (payment time, order status, etc.) to database. Initiate other services like delivery.<br/>
    **5. Close timeout order**<br/>
     Consumes the delay message. If payment is not completed within limited time, close the order and revert the stock.

- ## Reliability and High Availability
  The system uses cache and asynchronous processing with message queue to handle massive amounts of requests as explained above. This greatly improves the system's performance and capability of handling a high volume of requests. However, extreme cases still need to be taken care of and taken into consideration when designing the system. What if the number of requests is more than the maximum number of requests that the system can handle? What should we do to prevent the system from breaking down in this extreme case?<br/>
  The system uses open-source framework Sentinel to control the flow. The principle is that it monitors the flow (QPS), the number of concurrent threads or some other indices. It controls the flow when the specified threshold is reached to protect the system from breaking down, which guarantees reliability and high availability of the system.

- ## Prevention of Overselling
  Stock needs to be guaranteed accurately, that is, we cannot oversell a commodity. For example, if we have 100 stock available, we cannot sell more than 100. The overselling issue would occur if we read available stock directly from database and there were multiple threads running. This issue could be resolved by applying optimistic lock to database, that is, query available stock, check available stock and deduct stock. However, if we read from and write to database for every request, database will break down when there are massive amount of requests sent to the system (explained above). Lua Scripting supported by Redis is a better solution to prevent overselling.
  ### Solution
    **1.** Cache the stock information to protect database from crashing due to a large number of requests.<br/>
    **2.** Checking and deducting stock stored in Redis cache are two operations. Use Lua scripts to combine the two operations into one, which guarantees atomicity. This ensures the accuracy of the stock and prevent overselling in high concurrency environment.<br/>
    **3.** At the time of creating an order, database will double-check the stock (stored in database) to reconfirm that there is available stock to prevent overselling.

# Bottleneck
### Data Consistency Between Cache and Database
**Note**: The situation described in Analysis section below did not actually happen when I tested. But theoretically, it could happen when the system encounters very high concurrency. This is a potential issue that could affect user experience. So it needs to be taken care of.

**Background**: Commodity stock is stored in MySQL database. It is also stored in Redis cache because we need to check whether there is stock available when a user is trying to place an order to avoid overselling. Since if we read available stock from database each time a user tries to place an order, database will probably crash down (database stores data on disk while cache stores in memory). Hence, instead of reading from database each time, we read stock information from cache. This explains why stock information is stored in both cache and database. And hence, data consistency between cache and database must be taken care of.

**Scenario**: There is no stock of some commodity right now. A user has placed an order of this commodity but does not complete payment within limited time, then this order is canceled and the number of stock is reverted to 1. At the same time, another thread that handles a request of purchasing this commodity from another user gets executed.

**Analysis**: The number of stock reverted to 1 consists of two steps: (1) Update the stock in cache (2) Update the stock in database. <br/>
Case 1: Update database first and then update cache <br/>
Assume database update has just been done. So the stock stored in database is 1 while the stock stored in cache is still 0. Now, a context switch happens. The thread that handles request of purchasing the commodity from another user starts getting executed. Here comes the problem. When the new thread that is running checks if there is available stock by reading from cache, the result says there is no available stock because the stock stored in cache has not been updated yet and is still 0. However, actually there is one stock available now because an order was canceled and stock was reverted just now. In this situation, data inconsistency would occur and user would not be able to place an order even though there is one stock available. <br/>
Case 2: Update cache first and then update database <br/>
Assume cache update has just been done. So the stock stored in cache is 1 while the stock stored in database is still 0. Now, a context switch happens. The thread that handles request of purchasing the commodity from another user starts getting executed. Here comes the problem. When the new thread that is running checks if there is available stock by reading from cache, the result says there is one stock available and deducts the stock stored in cache to 0. However, the order service will double check the stock stored in database before creating an order, and the stock stored in database is still 0 since it has not been updated when context switch happens. At that point, the order service will return no stock available and fail to create an order even though there is actually one stock available. In this situation, data inconsistency would occur and user would not be able to place an order even though there is one stock available.

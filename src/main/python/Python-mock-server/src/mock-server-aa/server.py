import asyncio
import json
import logging
import time
import uuid

from sanic import Sanic, response
from scipy.stats import norm

app = Sanic(name="tea-py-service")
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] - %(message)s')
logger = logging.getLogger("Server")


# This is an asynchronous handler, it spends most of the time in the event loop.
# It wakes up every second 1 to print and finally returns after 3 seconds.
# This does let other handlers to be executed in the same processes while
# from the point of view of the client it took 3 seconds to complete.

@app.route('/boil-water', methods=['POST'])
async def boil_water(request):
    latency_ms = await sleep(50, 1000)
    input = request.json
    id = str(uuid.uuid4())
    return response.json({"water": {"time_taken": latency_ms,
                           "quantity": input["quantity"],
                           "id": id
                           },
                 "_input": input})


@app.route('/brew-tea', methods=['POST'])
async def brew_tea(request):
    latency_ms = await sleep(25, 500)
    input = request.json
    id = str(uuid.uuid4())
    return response.json({"tea": {"time_taken": latency_ms,
                         "quantity": input["quantity"],
                         "type": input["type"],
                         "name": input["name"],
                         "water": input["water"],
                         "id": id
                         },
                 "_input": input})


@app.route('/boil-milk', methods=['POST'])
async def boil_milk(request):
    latency_ms = await sleep(100, 2000)
    input = request.json
    id = str(uuid.uuid4())
    return response.json({"milk": {"time_taken": latency_ms,
                          "quantity": input["quantity"],
                          "type": input["type"],
                          "id": id
                          },
                 "_input": input})


@app.route('/combine-milk-tea', methods=['POST'])
async def combine_milk_tea(request):
    latency_ms = await sleep(10, 100)
    input = request.json
    id = str(uuid.uuid4())
    return response.json({"milk_tea": {"time_taken": latency_ms,
                              "quantity": input["milk"]["quantity"] + input["tea"]["quantity"],
                              "tea": input["tea"],
                              "milk": input["milk"],
                              "id": id
                              },
                 "_input": input})


async def sleep(avg: int, p9999: int):
    await asyncio.sleep(0.1)
    return 100
    """
    #s = 0.954
    latency_ms = norm.rvs(size=1, loc=avg, scale=(p9999 - avg) / 6).tolist()[0]
    if latency_ms < 0:
        latency_ms = -1 * latency_ms
    latency_sec = latency_ms/1000
    await asyncio.sleep(latency_sec)
    return latency_ms
    """


@app.middleware('request')
async def add_start_time(request):
    request.ctx.start_time = time.time()


@app.middleware('response')
async def add_spent_time(request, response):
    spend_time = round((time.time() - request.ctx.start_time) * 1000)
    logger.info("{} {} {} {}ms request={}  response={}".format(request.method,
                                                               response.status,
                                                               request.path,
                                                               spend_time,
                                                               json.dumps(request.json),
                                                               response.status))



if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080, debug=True)

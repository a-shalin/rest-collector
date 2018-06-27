import requests

period = {"begDate":"2018-01-01", "endDate":"2018-12-31"}

session = requests.Session()
resp = session.get("http://localhost:8080/stat", params=period, headers={"Content-Type" : "application/json;charset=UTF-8"})

resp.raise_for_status()

print(resp.json())

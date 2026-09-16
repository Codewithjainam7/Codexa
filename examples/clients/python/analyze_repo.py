import requests
res = requests.post("http://localhost:8080/api/v1/analysis/git", json={"url": "https://github.com/org/repo"})
print(res.json())

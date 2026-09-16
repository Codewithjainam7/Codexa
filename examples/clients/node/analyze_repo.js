const res = await fetch("http://localhost:8080/api/v1/analysis/jobs");
console.log(await res.json());

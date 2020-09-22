import http from 'k6/http';
import {check, sleep} from 'k6';

function call() {
    const mode = __ENV.MODE
    const url = `http://localhost:8080/${mode}`
    const headers = {'Content-Type' : 'application/json'}
    const request = {
        "tea": {
            "type": "black",
            "quantity": 100,
            "name": "Darjeeling Bloom"
        },
        "milk": {
            "quantity": 100,
            "type": "Skim"
        }
    }
    let response = http.request('POST', url, JSON.stringify(request), {headers: headers});
    check(response, {"response is successful" : r => r.status == 200})

}

// main entry point.
export default function() {
    call()
}
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    house_detail: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 20 },
        { duration: '60s', target: 50 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:9000';
const token = __ENV.ACCESS_TOKEN;
const houseId = __ENV.HOUSE_ID || '1';

export default function () {
  const response = http.get(`${baseUrl}/cloud/api/houses/${houseId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  check(response, { 'detail returns 200': (r) => r.status === 200 });
  sleep(0.2);
}

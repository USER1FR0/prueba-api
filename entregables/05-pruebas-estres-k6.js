// Prueba de estres con k6 (https://k6.io)
// Ejecutar:  k6 run 05-pruebas-estres-k6.js
//
// Simula carga sobre las consultas (lectura) y un registro ocasional.
// Ajusta BASE segun el entorno.

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8081';

export const options = {
  stages: [
    { duration: '30s', target: 20 },   // calentamiento
    { duration: '1m',  target: 100 },  // carga sostenida
    { duration: '30s', target: 200 },  // pico
    { duration: '30s', target: 0 },    // enfriamiento
  ],
  thresholds: {
    http_req_failed:   ['rate<0.01'],    // < 1% de errores
    http_req_duration: ['p(95)<800'],    // 95% bajo 800ms
  },
};

function curpAleatoria() {
  const n = Math.floor(Math.random() * 1e6).toString().padStart(6, '0');
  return `KPRX${n}HGTXYZ09`;
}

export default function () {
  // 80% lecturas
  const r1 = http.get(`${BASE}/clientes`);
  check(r1, { 'GET /clientes 200': (r) => r.status === 200 });

  const r2 = http.get(`${BASE}/clientes?activo=true`);
  check(r2, { 'GET activos 200': (r) => r.status === 200 });

  // 20% escritura (registro con datos unicos)
  if (Math.random() < 0.2) {
    const curp = curpAleatoria();
    const payload = JSON.stringify({
      nombre: 'Carga', apellidoPaterno: 'Prueba', apellidoMaterno: 'Estres',
      fechaNacimiento: '1990-01-01', curp: curp, rfc: curp.substring(0, 4) + '9001011A1'.substring(0, 9),
      sexo: 'OTRO', nacionalidad: 'Mexicana', estadoCivil: 'SOLTERO',
      correo: `${curp.toLowerCase()}@example.com`, telefonoMovil: '4770000000',
      ocupacion: 'Tester', empresa: 'LoadTest', ingresoMensual: 10000,
      domicilio: { calle: 'Calle', numeroExterior: '1', colonia: 'Centro',
        municipio: 'Leon', estado: 'GUANAJUATO', codigoPostal: '37000', pais: 'Mexico' },
    });
    const r3 = http.post(`${BASE}/clientes`, payload, { headers: { 'Content-Type': 'application/json' } });
    check(r3, { 'POST /clientes 201/409': (r) => r.status === 201 || r.status === 409 });
  }

  sleep(1);
}

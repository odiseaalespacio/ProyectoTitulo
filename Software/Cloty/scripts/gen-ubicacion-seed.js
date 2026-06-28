const fs = require('fs');
const path = require('path');

const base = path.join(__dirname, '..', 'cloty-api', 'src', 'main', 'resources', 'data');
const regions = JSON.parse(fs.readFileSync(path.join(base, 'chile-regiones.json'), 'utf8'));
const comunas = JSON.parse(fs.readFileSync(path.join(base, 'chile-comunas.json'), 'utf8'));
const out = path.join(__dirname, '..', 'cloty-ubicacion-seed.sql');

const esc = (s) => s.replace(/'/g, "''");
const lines = [
  '-- Seed regiones y comunas de Chile (CUT SUBDERE)',
  '-- Ejecutar después de cloty-mysql.sql:',
  '--   mysql -u root -p cloty < cloty-ubicacion-seed.sql',
  'SET NAMES utf8mb4;',
  '',
  'INSERT INTO region (codigo_region, nombre) VALUES',
  regions.map((r) => `  ('${r.codigo_region}', '${esc(r.nombre_region)}')`).join(',\n') + ';',
  '',
  'INSERT INTO comuna (codigo_comuna, codigo_region, nombre) VALUES',
  comunas.map((c) => `  ('${c.codigo_comuna}', '${c.codigo_region}', '${esc(c.nombre_comuna)}')`).join(',\n') + ';',
];
fs.writeFileSync(out, lines.join('\n'), 'utf8');
console.log(`Wrote ${out} (${regions.length} regions, ${comunas.length} comunas)`);

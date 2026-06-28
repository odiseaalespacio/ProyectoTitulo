import json
from pathlib import Path

base = Path(__file__).resolve().parent.parent / "cloty-api" / "src" / "main" / "resources" / "data"
regions = json.loads((base / "chile-regiones.json").read_text(encoding="utf-8"))
comunas = json.loads((base / "chile-comunas.json").read_text(encoding="utf-8"))
out = Path(__file__).resolve().parent.parent / "cloty-ubicacion-seed.sql"

lines = [
    "-- Seed regiones y comunas de Chile (CUT SUBDERE)",
    "-- Ejecutar después de cloty-mysql.sql:",
    "--   mysql -u root -p cloty < cloty-ubicacion-seed.sql",
    "SET NAMES utf8mb4;",
    "",
    "INSERT INTO region (codigo_region, nombre) VALUES",
]
lines.append(",\n".join(
    f"  ('{r['codigo_region']}', '{r['nombre_region'].replace(chr(39), chr(39) + chr(39))}')"
    for r in regions
) + ";")
lines.append("")
lines.append("INSERT INTO comuna (codigo_comuna, codigo_region, nombre) VALUES")
rows = []
for c in comunas:
    nombre = c["nombre_comuna"].replace("'", "''")
    rows.append(f"  ('{c['codigo_comuna']}', '{c['codigo_region']}', '{nombre}')")
lines.append(",\n".join(rows) + ";")
out.write_text("\n".join(lines), encoding="utf-8")
print(f"Wrote {out} ({len(regions)} regions, {len(comunas)} comunas)")

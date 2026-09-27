#!/usr/bin/env python3
"""Import mysqldump into SQLite — row-by-row inserts, preserves MySQL IDs."""

from __future__ import annotations

import argparse
import re
import sqlite3
import sys
from pathlib import Path

DEFAULT_DROPBOX_PATH = "Kavuma-Medical-Clinic/kmc-latest.db"
DRIFT_SCHEMA_VERSION = 5

CREATE_RE = re.compile(
    r"CREATE TABLE `([^`]+)` \((.*?)\) ENGINE=",
    re.DOTALL | re.IGNORECASE,
)
COL_LINE_RE = re.compile(r"^\s*`([^`]+)`\s+(\w+)")
INSERT_RE = re.compile(
    r"INSERT INTO `([^`]+)` VALUES\s*(.+?);\s*(?:\n|$)",
    re.DOTALL | re.IGNORECASE,
)


def mysql_col_to_sqlite(mysql_type: str) -> str:
    t = mysql_type.lower()
    if t in ("bigint", "int", "integer", "tinyint", "smallint", "mediumint"):
        return "INTEGER"
    if t in ("decimal", "double", "float", "real"):
        return "REAL"
    if t in ("blob", "longblob", "mediumblob", "tinyblob"):
        return "BLOB"
    return "TEXT"


def parse_create_tables(sql: str) -> dict[str, list[tuple[str, str]]]:
    tables: dict[str, list[tuple[str, str]]] = {}
    for match in CREATE_RE.finditer(sql):
        name = match.group(1)
        if name.endswith("_seq"):
            continue
        cols: list[tuple[str, str]] = []
        for line in match.group(2).splitlines():
            line = line.strip().rstrip(",")
            if not line.startswith("`"):
                continue
            m = COL_LINE_RE.match(line)
            if m:
                cols.append((m.group(1), m.group(2)))
        if cols:
            tables[name] = cols
    return tables


def build_create_sql(table: str, cols: list[tuple[str, str]]) -> str:
    parts = [f'"{n}" {mysql_col_to_sqlite(t)}' for n, t in cols]
    return f'CREATE TABLE IF NOT EXISTS "{table}" ({", ".join(parts)});'


def normalize_value_blob(s: str) -> str:
    s = re.sub(r"_binary\s+'\\0'", "0", s)
    s = re.sub(r"_binary\s+'\\1'", "1", s)
    s = re.sub(r"_binary\s+'\x00'", "0", s)
    s = re.sub(r"_binary\s+'\x01'", "1", s)
    # MySQL bit as quoted control chars in dump
    s = s.replace("'\\0'", "0").replace("'\\1'", "1")
    s = s.replace("'\x00'", "0").replace("'\x01'", "1")
    s = s.replace("'\x01'", "1")
    return s


def split_value_tuples(values_sql: str) -> list[str]:
    """Split `(a,b),(c,d)` into ['(a,b)', '(c,d)']."""
    tuples: list[str] = []
    depth = 0
    in_string = False
    escape = False
    start = 0
    i = 0
    while i < len(values_sql):
        ch = values_sql[i]
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == "'":
                in_string = False
        else:
            if ch == "'":
                in_string = True
            elif ch == "(":
                if depth == 0:
                    start = i
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    tuples.append(values_sql[start : i + 1])
            elif ch == "," and depth == 0:
                pass
        i += 1
    return tuples


def extract_inserts(sql: str) -> list[tuple[str, str]]:
    results: list[tuple[str, str]] = []
    for m in INSERT_RE.finditer(sql):
        table = m.group(1)
        values_blob = m.group(2).strip()
        results.append((table, values_blob))
    return results


def ensure_app_tables(conn: sqlite3.Connection, dropbox_path: str = DEFAULT_DROPBOX_PATH) -> None:
    """Mother-app tables required alongside imported Quarkus schema."""
    escaped_path = dropbox_path.replace("'", "''")
    conn.executescript(
        f"""
        CREATE TABLE IF NOT EXISTS backup_settings_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            facility_name TEXT NOT NULL DEFAULT 'Kavuma Medical Center',
            local_backup_root TEXT NOT NULL DEFAULT 'MediCenter-Backups',
            dropbox_backup_path TEXT NOT NULL DEFAULT '{escaped_path}',
            local_backup_enabled INTEGER NOT NULL DEFAULT 1,
            dropbox_backup_enabled INTEGER NOT NULL DEFAULT 0,
            scheduled_enabled INTEGER NOT NULL DEFAULT 1,
            local_interval_minutes INTEGER NOT NULL DEFAULT 6,
            dropbox_interval_minutes INTEGER NOT NULL DEFAULT 10,
            last_local_backup_at INTEGER,
            last_dropbox_backup_at INTEGER,
            dropbox_connected INTEGER NOT NULL DEFAULT 0,
            last_local_backup_status TEXT,
            last_dropbox_backup_status TEXT,
            last_backup_error TEXT
        );
        CREATE TABLE IF NOT EXISTS backup_log_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source TEXT NOT NULL,
            status TEXT NOT NULL,
            detail TEXT,
            file_size_bytes INTEGER,
            created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
        );
        CREATE TABLE IF NOT EXISTS clinic_meta_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            key TEXT NOT NULL UNIQUE,
            value TEXT NOT NULL,
            updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
        );
        CREATE TABLE IF NOT EXISTS drift_schema_versions (
            schema_version INTEGER NOT NULL
        );
        DELETE FROM drift_schema_versions;
        INSERT INTO drift_schema_versions (schema_version) VALUES ({DRIFT_SCHEMA_VERSION});
        """
    )


def import_dump(sql_path: Path, out_db: Path, dropbox_path: str = DEFAULT_DROPBOX_PATH) -> dict[str, int]:
    if not sql_path.is_file():
        raise FileNotFoundError(f"SQL dump not found: {sql_path}")

    print(f"Reading {sql_path}...")
    sql = sql_path.read_text(encoding="utf-8", errors="replace")
    if not sql.strip():
        raise ValueError(f"SQL dump is empty: {sql_path}")

    tables = parse_create_tables(sql)
    if not tables:
        raise ValueError(f"No CREATE TABLE statements found in {sql_path}")

    out_db.parent.mkdir(parents=True, exist_ok=True)
    if out_db.exists():
        out_db.unlink()

    conn = sqlite3.connect(out_db)
    conn.execute("PRAGMA foreign_keys = OFF;")
    cur = conn.cursor()

    for table_name in sorted(tables.keys()):
        cur.execute(build_create_sql(table_name, tables[table_name]))

    ok_rows = fail_rows = 0
    failed_tables: set[str] = set()

    for table, values_blob in extract_inserts(sql):
        if table.endswith("_seq") or table not in tables:
            continue
        col_names = ", ".join(f'"{c[0]}"' for c in tables[table])
        for tuple_sql in split_value_tuples(values_blob):
            row_sql = normalize_value_blob(tuple_sql)
            try:
                cur.execute(
                    f'INSERT OR REPLACE INTO "{table}" ({col_names}) VALUES {row_sql};'
                )
                ok_rows += 1
            except sqlite3.Error as e:
                fail_rows += 1
                if table not in failed_tables:
                    failed_tables.add(table)
                    print(f"WARN {table}: {e}", file=sys.stderr)

    ensure_app_tables(conn, dropbox_path)
    conn.commit()
    conn.close()

    if not out_db.is_file() or out_db.stat().st_size == 0:
        raise RuntimeError(f"SQLite export failed — output missing or empty: {out_db}")

    return {
        "tables": len(tables),
        "rows_ok": ok_rows,
        "rows_failed": fail_rows,
        "bytes": out_db.stat().st_size,
    }


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Convert MySQL mysqldump to mother-app SQLite (kmc-latest.db)."
    )
    parser.add_argument("sql_dump", type=Path, nargs="?", help="Path to .sql mysqldump file")
    parser.add_argument("output_db", type=Path, nargs="?", help="Path to write SQLite .db file")
    parser.add_argument(
        "--dropbox-path",
        default=DEFAULT_DROPBOX_PATH,
        help="Default Dropbox path stored in backup_settings_entries",
    )
    args = parser.parse_args()

    if args.sql_dump is None or args.output_db is None:
        sql_dump = Path(r"C:\Users\hp\Downloads\new kmc mysql db\kmc.db")
        output_db = Path(__file__).resolve().parent.parent.parent / "assets" / "seed" / "kmc_mysql_import.db"
    else:
        sql_dump = args.sql_dump
        output_db = args.output_db

    stats = import_dump(sql_dump, output_db, args.dropbox_path)
    print(
        f"SQLite export OK: {output_db} "
        f"({stats['bytes']} bytes, {stats['tables']} tables, "
        f"{stats['rows_ok']} rows, {stats['rows_failed']} failures)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

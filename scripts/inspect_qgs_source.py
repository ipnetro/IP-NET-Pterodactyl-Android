#!/usr/bin/env python3
"""Inspect the supplied decoded QGS tree without compiling or producing an APK."""
from pathlib import Path
from zipfile import ZipFile
import re

archive = Path("qgs-ipnet-modified-source.zip")
out = Path("qgs-inspection")
out.mkdir(exist_ok=True)
if not archive.is_file():
    raise SystemExit("Missing qgs-ipnet-modified-source.zip")

with ZipFile(archive) as z:
    entries = [i for i in z.infolist() if not i.is_dir()]
    names = [i.filename for i in entries]
    (out / "all-files.txt").write_text("\n".join(names), encoding="utf-8")
    groups = {
        "manifests": [n for n in names if n.endswith("AndroidManifest.xml")],
        "value_resources": [n for n in names if "/res/values" in n and n.endswith(".xml")],
        "launcher_resources": [n for n in names if re.search(r"/res/(mipmap|drawable)", n)],
        "dex_and_smali": [n for n in names if n.endswith(".dex") or "/smali" in n],
        "network_and_config": [n for n in names if any(k in n.lower() for k in ("network", "config", "baseurl", "endpoint"))],
    }
    report = ["QGS SOURCE INSPECTION — NO APK BUILD", f"Archive entries: {len(entries)}", ""]
    for title, values in groups.items():
        report += [f"[{title}] ({len(values)})", *values[:500], ""]
    needles = ("ptero.ip-net.ro", "qgs.hosting.com", "app_name", "ic_launcher_background", "http://", "https://")
    matches = []
    for info in entries:
        if info.file_size > 2_000_000:
            continue
        try:
            text = z.read(info).decode("utf-8")
        except (UnicodeDecodeError, OSError, RuntimeError):
            continue
        for line_no, line in enumerate(text.splitlines(), 1):
            if any(term in line for term in needles):
                matches.append(f"{info.filename}:{line_no}: {line[:500]}")
    report += ["[text_matches]", *matches[:3000], ""]
    (out / "inspection-report.txt").write_text("\n".join(report), encoding="utf-8")
    print("\n".join(report[:120]))
    print("[KEY TEXT MATCHES]")
    for item in matches:
        print(item)

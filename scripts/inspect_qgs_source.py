#!/usr/bin/env python3
from pathlib import Path
from zipfile import ZipFile
import os, re
root=Path(os.environ["RUNNER_TEMP"])
archive=Path("qgs-ipnet-modified-source.zip")
out=root/"qgs-inspection"
out.mkdir(exist_ok=True)
with ZipFile(archive) as z:
    names=z.namelist()
    (out/"zip-files.txt").write_text("\n".join(names), encoding="utf-8")
    candidates=[n for n in names if n.endswith(("AndroidManifest.xml","res/values/strings.xml","res/values/styles.xml","res/values/colors.xml"))]
    for n in candidates:
        try:
            data=z.read(n)
            # Apktool decoded XML is text; binary manifest is reported as such.
            try: text=data.decode("utf-8")
            except UnicodeDecodeError: text="[binary XML; decode with apktool]\n"
            (out/(Path(n).name+"-"+str(len(list(out.iterdir())))+".txt")).write_text("SOURCE: "+n+"\n"+text,encoding="utf-8")
        except Exception as e:
            (out/"errors.txt").write_text(str(e),encoding="utf-8")
print("ZIP entries:",len(names))
print("Manifest/resources candidates:")
for n in candidates: print(n)

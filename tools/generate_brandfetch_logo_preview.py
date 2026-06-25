#!/usr/bin/env python3
"""Generate an HTML preview that directly embeds Brandfetch Logo API images.

Brandfetch Logo API requires a client id and is intended for direct image
embedding. This preview intentionally does not download, cache, or package the
Brandfetch images.
"""

from __future__ import annotations

import html
import os
import sys
from pathlib import Path
from urllib.parse import quote

from download_tv_brand_logos import BRAND_DOMAINS, BRAND_ROOT, ROOT, brand_key


OUTPUT_PATH = ROOT / "brandfetch-logo-preview.html"
DEFAULT_BRANDFETCH_CLIENT_ID = "1id2KPHxY5IIyj3mB1s"
BRANDFETCH_URL = (
    "https://cdn.brandfetch.io/domain/{domain}/w/400/h/400/theme/dark/fallback/404/type/logo.png?c={client_id}"
)


def brand_rows(client_id: str) -> list[str]:
    rows = []
    brands = sorted(
        path.name for path in BRAND_ROOT.iterdir()
        if path.is_dir() and brand_key(path.name) in BRAND_DOMAINS
    )
    for brand in brands:
        domain = BRAND_DOMAINS[brand_key(brand)]
        image_url = BRANDFETCH_URL.format(
            domain=quote(domain, safe="."),
            client_id=quote(client_id),
        )
        rows.append(
            f"""
            <article class="card">
              <div class="logo-box">
                <img src="{html.escape(image_url)}" alt="{html.escape(brand)} logo" loading="lazy" />
              </div>
              <div class="brand">{html.escape(brand)}</div>
              <div class="domain">{html.escape(domain)}</div>
            </article>
            """
        )
    return rows


def write_preview(client_id: str) -> None:
    missing_id = not client_id or client_id == "BRANDFETCH_CLIENT_ID"
    warning = ""
    if missing_id:
        warning = """
        <section class="warning">
          Set <code>BRANDFETCH_CLIENT_ID</code> before generating this file.
          The current file uses a placeholder and Brandfetch images may return 403.
        </section>
        """

    content = f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Brandfetch TV Logo Preview</title>
  <style>
    :root {{
      color-scheme: light;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      background: #f7f8f6;
      color: #202124;
    }}
    body {{
      margin: 0;
      padding: 28px;
    }}
    header {{
      max-width: 1120px;
      margin: 0 auto 20px;
    }}
    h1 {{
      margin: 0 0 8px;
      font-size: 24px;
    }}
    p {{
      margin: 0;
      color: #6f756f;
      line-height: 1.5;
    }}
    .warning {{
      max-width: 1120px;
      margin: 0 auto 20px;
      padding: 12px 14px;
      border: 1px solid #d9a441;
      border-radius: 10px;
      background: #fff4d8;
      color: #4c3a12;
    }}
    .grid {{
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 14px;
      max-width: 1120px;
      margin: 0 auto;
    }}
    .card {{
      min-height: 150px;
      border: 1px solid #dddfd7;
      border-radius: 12px;
      background: white;
      padding: 12px;
    }}
    .logo-box {{
      height: 78px;
      display: grid;
      place-items: center;
      border-radius: 10px;
      background: #f2f4f1;
      overflow: hidden;
    }}
    img {{
      max-width: 116px;
      max-height: 58px;
      object-fit: contain;
    }}
    .brand {{
      margin-top: 10px;
      font-size: 14px;
      font-weight: 700;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }}
    .domain {{
      margin-top: 3px;
      color: #6f756f;
      font-size: 12px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }}
  </style>
</head>
<body>
  <header>
    <h1>Brandfetch TV Logo Preview</h1>
    <p>This file directly embeds Brandfetch Logo API URLs for visual review. It does not download or package Brandfetch images.</p>
  </header>
  {warning}
  <main class="grid">
    {"".join(brand_rows(client_id))}
  </main>
</body>
</html>
"""
    OUTPUT_PATH.write_text(content, encoding="utf-8")


def main() -> int:
    client_id = os.environ.get("BRANDFETCH_CLIENT_ID", DEFAULT_BRANDFETCH_CLIENT_ID).strip()
    write_preview(client_id)
    print(OUTPUT_PATH)
    return 0


if __name__ == "__main__":
    sys.exit(main())
